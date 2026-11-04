package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.credentials.CredentialStorageFactory;
import com.dtsx.astra.cli.core.credentials.FileFallbackStorage;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.operations.LoginOperation.LoginResult;
import com.dtsx.astra.cli.utils.ShellUtils;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Browser login using the OAuth 2.0 Authorization Code flow with PKCE (RFC 7636, S256) and a
 * loopback redirect (RFC 8252).
 *
 * <ol>
 *   <li>Generate a random {@code code_verifier}; derive {@code code_challenge = BASE64URL(SHA256(verifier))}.</li>
 *   <li>Open {@code <session>/v2/oauth/authorize} in the browser with the challenge and a random {@code state}.</li>
 *   <li>Receive {@code ?code=&state=} on {@code http://localhost:<port>/callback}.</li>
 *   <li>Exchange the code + verifier at {@code POST <session>/v2/oauth/token} (back channel) for a JWT.</li>
 * </ol>
 */
@RequiredArgsConstructor
public class LoginOperation implements Operation<LoginResult> {
    private static final int TIMEOUT_SECONDS = 120;
    private static final String CALLBACK_PATH = "/callback";
    private static final String CLIENT_ID = "astra-cli";
    private static final String DEFAULT_LOGIN_URL = "https://astra.datastax.com/v1/login";
    /** Default RFC 8707 resource indicator path, appended to the session base URL. */
    private static final String DEFAULT_RESOURCE_PATH = "/v1/mcp";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String SUCCESS_HTML = """
        <!doctype html>
        <html><head><title>Astra CLI Login</title></head>
        <body style="font-family:sans-serif;text-align:center;padding-top:80px">
          <h2>✓ Login successful!</h2>
          <p>You can close this tab and return to the terminal.</p>
        </body></html>
        """;

    private static final String ERROR_HTML = """
        <!doctype html>
        <html><head><title>Astra CLI Login</title></head>
        <body style="font-family:sans-serif;text-align:center;padding-top:80px">
          <h2>✗ Login failed</h2>
          <p>The login response was invalid or was rejected. Return to the terminal for details.</p>
        </body></html>
        """;

    private final CliContext ctx;
    private final LoginRequest request;

    /**
     * An org+role pair chosen by the user (or passed via flags) after the PKCE login.
     * {@code orgId}/{@code roleId} are the UUIDs that go to POST /v2/oauth/client-token.
     * {@code orgName}/{@code roleName} are the display names stored in the profile for
     * {@code auth switch} to use without a network call.
     */
    public record OrgRoleSelection(String orgId, String orgName, String roleId, String roleName) {}

    /** One org entry returned by GET /v2/oauth/userinfo. */
    public record OrgInfo(String id, String name, List<RoleInfo> roles) {}

    /** One role entry within an org returned by GET /v2/oauth/userinfo. */
    public record RoleInfo(String id, String name) {}

    public record StorageChoice(String label, boolean useKeychain) {}

    public record LoginRequest(
        Optional<String> loginBaseUrl,
        Optional<ProfileName> profileName,
        boolean setAsDefault,
        Optional<String> orgFlag,
        Optional<String> roleFlag,
        Optional<String> resource,
        Optional<Boolean> useKeychain
    ) {
        /** Back-compat constructor for callers that supply resource indicator without storage choice. */
        public LoginRequest(Optional<String> loginBaseUrl, Optional<ProfileName> profileName, boolean setAsDefault, Optional<String> orgFlag, Optional<String> roleFlag, Optional<String> resource) {
            this(loginBaseUrl, profileName, setAsDefault, orgFlag, roleFlag, resource, Optional.empty());
        }

        /** Back-compat constructor for callers that don't supply a resource indicator (RFC 8707). */
        public LoginRequest(Optional<String> loginBaseUrl, Optional<ProfileName> profileName, boolean setAsDefault, Optional<String> orgFlag, Optional<String> roleFlag) {
            this(loginBaseUrl, profileName, setAsDefault, orgFlag, roleFlag, Optional.empty(), Optional.empty());
        }

        /** Back-compat constructor for tests that don't supply org/role flags yet. */
        public LoginRequest(Optional<String> loginBaseUrl, Optional<ProfileName> profileName, boolean setAsDefault) {
            this(loginBaseUrl, profileName, setAsDefault, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }
    }

    public sealed interface LoginResult {}
    public record LoginSuccess(ProfileName profileName, boolean isDefault) implements LoginResult {}
    public record LoginCancelled() implements LoginResult {}
    public record LoginError(String message) implements LoginResult {}

    /** What the loopback server captured from the browser redirect. */
    private record CallbackParams(Optional<String> code, Optional<String> state, Optional<String> error, Optional<String> errorDescription) {}

    @Override
    public LoginResult execute() {
        int port;
        try {
            port = findFreePort();
        } catch (IOException e) {
            return new LoginError("Could not find a free local port: " + e.getMessage());
        }

        val codeVerifier = generateCodeVerifier();
        val codeChallenge = computeCodeChallenge(codeVerifier);
        val expectedState = randomUrlSafe(32);
        val redirectUri = "http://localhost:" + port + CALLBACK_PATH;

        val callbackHolder = new AtomicReference<CallbackParams>();
        val latch = new CountDownLatch(1);

        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        } catch (IOException e) {
            return new LoginError("Could not start local callback server: " + e.getMessage());
        }

        server.createContext(CALLBACK_PATH, exchange -> {
            val params = parseQueryParams(Optional.ofNullable(exchange.getRequestURI().getRawQuery()).orElse(""));
            val callback = new CallbackParams(
                nonBlank(params.get("code")),
                nonBlank(params.get("state")),
                nonBlank(params.get("error")),
                nonBlank(params.get("error_description"))
            );

            val ok = callback.error().isEmpty()
                && callback.code().isPresent()
                && callback.state().map(s -> constantTimeEquals(s, expectedState)).orElse(false);

            val bytes = (ok ? SUCCESS_HTML : ERROR_HTML).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            exchange.sendResponseHeaders(200, bytes.length);
            try (val os = exchange.getResponseBody()) {
                os.write(bytes);
            }

            // Only the first callback counts; a stray request must not overwrite a valid one.
            if (callbackHolder.compareAndSet(null, callback)) {
                latch.countDown();
            }
        });

        server.setExecutor(Executors.newSingleThreadExecutor(r -> {
            val t = new Thread(r, "astra-cli-login-callback");
            t.setDaemon(true);
            return t;
        }));
        server.start();

        val sessionBase = sessionBase();
        val resource = request.resource().orElse(sessionBase + DEFAULT_RESOURCE_PATH);

        try {
            val authorizeUrl = sessionBase + "/v2/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + enc(CLIENT_ID)
                + "&redirect_uri=" + enc(redirectUri)
                + "&state=" + enc(expectedState)
                + "&code_challenge=" + enc(codeChallenge)
                + "&code_challenge_method=S256"
                + "&resource=" + enc(resource);

            ctx.console().println("Opening browser to complete login...");
            ctx.console().println("If a browser window does not open, visit:");
            ctx.console().println("  " + authorizeUrl);
            ctx.console().println();

            ShellUtils.openURL(authorizeUrl);

            if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                return new LoginCancelled();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new LoginCancelled();
        } finally {
            server.stop(1);
        }

        val callback = callbackHolder.get();

        if (callback.error().isPresent()) {
            if (callback.error().get().equals("access_denied")) {
                return new LoginError("Login was cancelled, or the organization/role selection was rejected (access_denied)"
                    + callback.errorDescription().map(d -> ": " + d).orElse("") + ". No profile was saved.");
            }
            return new LoginError("Login was rejected: " + callback.error().get()
                + callback.errorDescription().map(d -> " (" + d + ")").orElse(""));
        }
        if (callback.state().isEmpty() || !constantTimeEquals(callback.state().get(), expectedState)) {
            return new LoginError("Login callback state did not match; aborting to prevent a forged callback.");
        }
        if (callback.code().isEmpty()) {
            return new LoginError("No authorization code was received in the callback.");
        }

        val exchange = doExchange(sessionBase, callback.code().get(), codeVerifier, redirectUri, resource);
        if (exchange.error() != null) {
            return new LoginError(exchange.error());
        }
        val rawToken = exchange.accessToken();

        // Use the browser's org+role selection when the token response carries one; otherwise fetch the
        // org+role list and prompt (or use flags). Non-fatal on failure.
        val orgRole = (exchange.selection() != null)
            ? warnIfFlagsConflict(exchange.selection())
            : promptOrgRole(sessionBase, rawToken);

        val token = AstraToken.mkUnsafe(rawToken);
        val profileName = request.profileName().orElse(ProfileName.mkUnsafe("default"));

        val config = AstraConfig.readAstraConfigFile(ctx, null, true);
        config.modify(modCtx -> {
            modCtx.deleteProfile(profileName);
            modCtx.createProfile(profileName, token, AstraEnvironment.PROD);
            if (request.setAsDefault()) {
                modCtx.setDefault(profileName);
            }
        });

        provisionCsToken(config, sessionBase, rawToken, profileName, orgRole);

        return new LoginSuccess(profileName, request.setAsDefault());
    }

    // -----------------------------------------------------------------------------------------
    // PKCE primitives (RFC 7636)
    // -----------------------------------------------------------------------------------------

    /** 32 random bytes → 43 base64url chars, within the RFC 7636 §4.1 range of 43–128. */
    static String generateCodeVerifier() {
        return randomUrlSafe(32);
    }

    /** {@code BASE64URL-ENCODE(SHA256(ASCII(code_verifier)))}, no padding (RFC 7636 §4.2). */
    static String computeCodeChallenge(String codeVerifier) {
        try {
            val digest = MessageDigest.getInstance("SHA-256").digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by every Java platform", e);
        }
    }

    private static String randomUrlSafe(int byteCount) {
        val bytes = new byte[byteCount];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    // -----------------------------------------------------------------------------------------
    // Back-channel token exchange
    // -----------------------------------------------------------------------------------------

    /**
     * Exactly one of {@code accessToken} / {@code error} is non-null. {@code selection} is the browser's
     * org+role choice from the token response, or null when the response carries none.
     */
    private record ExchangeOutcome(String accessToken, String error, OrgRoleSelection selection) {
        static ExchangeOutcome ok(String accessToken, OrgRoleSelection selection) { return new ExchangeOutcome(accessToken, null, selection); }
        static ExchangeOutcome failure(String error) { return new ExchangeOutcome(null, error, null); }
    }

    private ExchangeOutcome doExchange(String sessionBase, String code, String codeVerifier, String redirectUri, String resource) {
        val form = "grant_type=authorization_code"
            + "&code=" + enc(code)
            + "&code_verifier=" + enc(codeVerifier)
            + "&client_id=" + enc(CLIENT_ID)
            + "&redirect_uri=" + enc(redirectUri)
            + "&resource=" + enc(resource);

        try {
            val httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            val httpReq = HttpRequest.newBuilder()
                .uri(URI.create(sessionBase + "/v2/oauth/token"))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
            val httpResp = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());

            val mapper = new ObjectMapper();
            if (httpResp.statusCode() != 200) {
                String detail = "HTTP " + httpResp.statusCode();
                try {
                    val node = mapper.readTree(httpResp.body());
                    val err = node.path("error").asText("");
                    val desc = node.path("error_description").asText("");
                    if (!err.isEmpty()) {
                        detail = err + (desc.isEmpty() ? "" : ": " + desc) + " (HTTP " + httpResp.statusCode() + ")";
                    }
                } catch (IOException ignored) {
                    // Non-JSON error body: the HTTP status is the best detail available and is reported below.
                }
                return ExchangeOutcome.failure("Token exchange failed: " + detail);
            }

            val body = mapper.readTree(httpResp.body());
            val accessToken = body.path("access_token").asText("");
            if (accessToken.isBlank()) {
                return ExchangeOutcome.failure("Token exchange succeeded but the response had no access_token.");
            }

            // Optional browser selection; a blank field counts as absent and names fall back to ids.
            val orgId = body.path("org_id").asText("");
            val roleId = body.path("role_id").asText("");
            OrgRoleSelection selection = null;
            if (!orgId.isBlank() && !roleId.isBlank()) {
                val orgName = body.path("org_name").asText("");
                val roleName = body.path("role_name").asText("");
                selection = new OrgRoleSelection(orgId, orgName.isBlank() ? orgId : orgName, roleId, roleName.isBlank() ? roleId : roleName);
            }
            return ExchangeOutcome.ok(accessToken, selection);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ExchangeOutcome.failure("Token exchange was interrupted.");
        } catch (IOException | IllegalArgumentException e) {
            return ExchangeOutcome.failure("Token exchange failed: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------------------------
    // Org+role selection (PKCE-02)
    // -----------------------------------------------------------------------------------------

    /** The browser selection wins; a {@code --org}/{@code --role} flag that matches it by neither id nor name (case-insensitive) is ignored with a warning. */
    private OrgRoleSelection warnIfFlagsConflict(OrgRoleSelection selection) {
        request.orgFlag()
            .filter(f -> !f.equalsIgnoreCase(selection.orgId()) && !f.equalsIgnoreCase(selection.orgName()))
            .ifPresent(f -> ctx.console().println("Warning: --org '" + f + "' is ignored; using the organization selected in the browser."));
        request.roleFlag()
            .filter(f -> !f.equalsIgnoreCase(selection.roleId()) && !f.equalsIgnoreCase(selection.roleName()))
            .ifPresent(f -> ctx.console().println("Warning: --role '" + f + "' is ignored; using the role selected in the browser."));
        return selection;
    }

    /**
     * Fetches the user's available orgs+roles from GET /v2/oauth/userinfo, then
     * either uses the {@code --org}/{@code --role} flags or prompts interactively.
     * Returns {@code null} if the org list cannot be fetched or selection fails
     * (provisionCsToken skips gracefully in that case).
     */
    private OrgRoleSelection promptOrgRole(String sessionBase, String rawToken) {
        val orgs = fetchOrgs(sessionBase, rawToken);
        if (orgs == null || orgs.isEmpty()) {
            return null;
        }

        // Resolve org: flag → single-org auto-select → interactive prompt.
        OrgInfo chosenOrg;
        if (request.orgFlag().isPresent()) {
            val orgFlagVal = request.orgFlag().get();
            chosenOrg = orgs.stream()
                .filter(o -> o.id().equals(orgFlagVal) || o.name().equalsIgnoreCase(orgFlagVal))
                .findFirst().orElse(null);
            if (chosenOrg == null) {
                ctx.console().println("Warning: --org '" + orgFlagVal + "' did not match any available org; skipping AstraCS provisioning.");
                return null;
            }
        } else if (orgs.size() == 1) {
            chosenOrg = orgs.getFirst();
        } else {
            val orgOptions = NEList.parse(orgs).orElse(null);
            if (orgOptions == null) return null;
            chosenOrg = ctx.console().select("Select an organization:")
                .options(orgOptions)
                .requireAnswer()
                .mapper(OrgInfo::name)
                .fallbackFlag("--org")
                .fix(ctx.originalArgs(), "--org <org>")
                .clearAfterSelection();
        }

        // Resolve role: flag → single-role auto-select → interactive prompt.
        val roles = chosenOrg.roles();
        if (roles.isEmpty()) {
            ctx.console().println("Warning: org '" + chosenOrg.name() + "' has no roles; skipping AstraCS provisioning.");
            return null;
        }

        RoleInfo chosenRole;
        if (request.roleFlag().isPresent()) {
            val roleFlagVal = request.roleFlag().get();
            chosenRole = roles.stream()
                .filter(r -> r.id().equals(roleFlagVal) || r.name().equalsIgnoreCase(roleFlagVal))
                .findFirst().orElse(null);
            if (chosenRole == null) {
                ctx.console().println("Warning: --role '" + roleFlagVal + "' did not match any available role; skipping AstraCS provisioning.");
                return null;
            }
        } else if (roles.size() == 1) {
            chosenRole = roles.getFirst();
        } else {
            val roleOptions = NEList.parse(roles).orElse(null);
            if (roleOptions == null) return null;
            chosenRole = ctx.console().select("Select a role for " + chosenOrg.name() + ":")
                .options(roleOptions)
                .requireAnswer()
                .mapper(RoleInfo::name)
                .fallbackFlag("--role")
                .fix(ctx.originalArgs(), "--role <role>")
                .clearAfterSelection();
        }

        return new OrgRoleSelection(chosenOrg.id(), chosenOrg.name(), chosenRole.id(), chosenRole.name());
    }

    /**
     * Calls GET /v2/oauth/userinfo to retrieve the user's available orgs+roles.
     * Returns null on any error (caller treats null as "skip provisioning").
     */
    private List<OrgInfo> fetchOrgs(String sessionBase, String rawToken) {
        try {
            val httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            val httpReq = HttpRequest.newBuilder()
                .uri(URI.create(sessionBase + "/v2/oauth/userinfo"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + rawToken)
                .GET()
                .build();
            val httpResp = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());

            if (httpResp.statusCode() != 200) {
                ctx.console().println("Warning: could not fetch org list (HTTP " + httpResp.statusCode() + "); skipping org/role selection.");
                return null;
            }

            val mapper = new ObjectMapper();
            val root = mapper.readTree(httpResp.body());
            val orgsNode = root.path("orgs");
            if (!orgsNode.isArray()) return List.of();

            val result = new java.util.ArrayList<OrgInfo>();
            for (val orgNode : orgsNode) {
                val orgId = orgNode.path("id").asText("");
                val orgName = orgNode.path("name").asText("");
                val rolesList = new java.util.ArrayList<RoleInfo>();
                for (val roleNode : orgNode.path("roles")) {
                    rolesList.add(new RoleInfo(roleNode.path("id").asText(""), roleNode.path("name").asText("")));
                }
                result.add(new OrgInfo(orgId, orgName, List.copyOf(rolesList)));
            }
            return List.copyOf(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException | IllegalArgumentException e) {
            ctx.console().println("Warning: failed to fetch org list: " + e.getMessage());
            return null;
        }
    }

    // -----------------------------------------------------------------------------------------
    // AstraCS provisioning (non-fatal: the JWT login itself already succeeded)
    // -----------------------------------------------------------------------------------------

    private void provisionCsToken(AstraConfig config, String sessionBase, String rawToken,
                                  ProfileName profileName, OrgRoleSelection orgRole) {
        if (orgRole == null) {
            return;
        }

        try {
            val mapper = new ObjectMapper();
            val bodyJson = mapper.writeValueAsString(
                Map.of("orgId", orgRole.orgId(), "roles", List.of(orgRole.roleId())));

            val httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            val httpReq = HttpRequest.newBuilder()
                .uri(URI.create(sessionBase + "/v2/oauth/client-token"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + rawToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();
            val httpResp = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());

            if (httpResp.statusCode() != 200) {
                ctx.console().println("Warning: AstraCS token provisioning failed (HTTP " + httpResp.statusCode() + ").");
                return;
            }

            val csTokenValue = mapper.readTree(httpResp.body()).path("token").asText(null);
            if (csTokenValue == null || csTokenValue.isBlank()) {
                ctx.console().println("Warning: AstraCS token provisioning returned no token field.");
                return;
            }

            val csToken = AstraToken.mkUnsafe(csTokenValue);
            val useKeychain = promptStorageChoice();
            config.modify(modCtx -> {
                modCtx.setCsToken(profileName, csToken, useKeychain);
                modCtx.setOrgRoleMeta(profileName, orgRole.orgId(), orgRole.orgName(), orgRole.roleId(), orgRole.roleName());
                if (request.setAsDefault()) {
                    modCtx.setCsToken(ProfileName.mkUnsafe("default"), csToken, useKeychain);
                    modCtx.setOrgRoleMeta(ProfileName.mkUnsafe("default"), orgRole.orgId(), orgRole.orgName(), orgRole.roleId(), orgRole.roleName());
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ctx.console().println("Warning: AstraCS token provisioning was interrupted.");
        } catch (IOException | IllegalArgumentException e) {
            ctx.console().println("Warning: AstraCS token provisioning failed: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------------------------
    // Credential Storage Selection
    // -----------------------------------------------------------------------------------------

    private boolean promptStorageChoice() {
        val manager = CredentialStorageFactory.get();
        if (manager instanceof FileFallbackStorage) {
            return false;
        }
        if (request.useKeychain().isPresent()) {
            return request.useKeychain().get();
        }
        if (ctx.isNotTty()) {
            return true;
        }

        val optionsList = List.of(
            new StorageChoice(manager.name() + " (Secure OS storage)", true),
            new StorageChoice("Configuration file (~/.astrarc)", false)
        );
        val options = NEList.parse(optionsList).orElse(null);
        if (options == null) {
            return true;
        }

        val chosen = ctx.console().select("Where would you like to save your credentials?")
            .options(options)
            .defaultOption(options.getFirst())
            .mapper(StorageChoice::label)
            .fallbackFlag("--keychain")
            .fix(ctx.originalArgs(), "--keychain")
            .clearAfterSelection();

        return chosen != null && chosen.useKeychain();
    }

    // -----------------------------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------------------------

    /**
     * Session-service base URL. {@code --login-url} / {@code ASTRA_LOGIN_URL} historically point at
     * {@code .../v1/login}; strip the versioned path so we can address {@code /v2/oauth/*}.
     */
    private String sessionBase() {
        val configured = request.loginBaseUrl()
            .or(() -> Optional.ofNullable(System.getenv("ASTRA_LOGIN_URL")))
            .orElse(DEFAULT_LOGIN_URL);
        return configured.replaceFirst("/v?[0-9]+/.*$", "");
    }

    private static Optional<String> nonBlank(String s) {
        return (s == null || s.isBlank()) ? Optional.empty() : Optional.of(s);
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static int findFreePort() throws IOException {
        try (val socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

    private static Map<String, String> parseQueryParams(String rawQuery) {
        val result = new HashMap<String, String>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return result;
        }
        for (val pair : rawQuery.split("&")) {
            val kv = pair.split("=", 2);
            if (kv.length == 2) {
                result.putIfAbsent(
                    URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                );
            }
        }
        return result;
    }
}
