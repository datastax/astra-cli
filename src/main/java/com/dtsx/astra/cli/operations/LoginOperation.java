package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.operations.LoginOperation.LoginResult;
import com.dtsx.astra.cli.utils.ShellUtils;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.fasterxml.jackson.core.type.TypeReference;
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

    public record LoginRequest(
        Optional<String> loginBaseUrl,
        Optional<ProfileName> profileName,
        boolean setAsDefault
    ) {}

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

        try {
            val authorizeUrl = sessionBase + "/v2/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + enc(CLIENT_ID)
                + "&redirect_uri=" + enc(redirectUri)
                + "&state=" + enc(expectedState)
                + "&code_challenge=" + enc(codeChallenge)
                + "&code_challenge_method=S256";

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
            return new LoginError("Login was rejected: " + callback.error().get()
                + callback.errorDescription().map(d -> " (" + d + ")").orElse(""));
        }
        if (callback.state().isEmpty() || !constantTimeEquals(callback.state().get(), expectedState)) {
            return new LoginError("Login callback state did not match; aborting to prevent a forged callback.");
        }
        if (callback.code().isEmpty()) {
            return new LoginError("No authorization code was received in the callback.");
        }

        val exchange = doExchange(sessionBase, callback.code().get(), codeVerifier, redirectUri);
        if (exchange.error() != null) {
            return new LoginError(exchange.error());
        }
        val rawToken = exchange.accessToken();

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

        provisionCsToken(config, sessionBase, rawToken, profileName);

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

    /** Exactly one of {@code accessToken} / {@code error} is non-null. */
    private record ExchangeOutcome(String accessToken, String error) {
        static ExchangeOutcome ok(String accessToken) { return new ExchangeOutcome(accessToken, null); }
        static ExchangeOutcome failure(String error) { return new ExchangeOutcome(null, error); }
    }

    private ExchangeOutcome doExchange(String sessionBase, String code, String codeVerifier, String redirectUri) {
        val form = "grant_type=authorization_code"
            + "&code=" + enc(code)
            + "&code_verifier=" + enc(codeVerifier)
            + "&client_id=" + enc(CLIENT_ID)
            + "&redirect_uri=" + enc(redirectUri);

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

            val accessToken = mapper.readTree(httpResp.body()).path("access_token").asText("");
            if (accessToken.isBlank()) {
                return ExchangeOutcome.failure("Token exchange succeeded but the response had no access_token.");
            }
            return ExchangeOutcome.ok(accessToken);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ExchangeOutcome.failure("Token exchange was interrupted.");
        } catch (IOException | IllegalArgumentException e) {
            return ExchangeOutcome.failure("Token exchange failed: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------------------------
    // AstraCS provisioning (non-fatal: the JWT login itself already succeeded)
    // -----------------------------------------------------------------------------------------

    private void provisionCsToken(AstraConfig config, String sessionBase, String rawToken, ProfileName profileName) {
        val payload = decodeJwtPayload(rawToken);
        if (payload.isEmpty()) {
            ctx.console().println("Warning: could not decode the access token; skipping AstraCS token provisioning.");
            return;
        }

        val orgId = payload.get("org") instanceof String s ? s : null;
        val roles = payload.get("grp") instanceof List<?> l ? l : List.of();
        if (orgId == null || orgId.isBlank() || roles.isEmpty()) {
            return;
        }

        try {
            val mapper = new ObjectMapper();
            val bodyJson = mapper.writeValueAsString(Map.of("orgId", orgId, "roles", roles));

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
            config.modify(modCtx -> {
                modCtx.setCsToken(profileName, csToken);
                if (request.setAsDefault()) {
                    modCtx.setCsToken(ProfileName.mkUnsafe("default"), csToken);
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ctx.console().println("Warning: AstraCS token provisioning was interrupted.");
        } catch (IOException | IllegalArgumentException e) {
            ctx.console().println("Warning: AstraCS token provisioning failed: " + e.getMessage());
        }
    }

    private static Map<String, Object> decodeJwtPayload(String jwt) {
        try {
            val parts = jwt.split("\\.");
            if (parts.length < 2) return Map.of();
            // Base64 URL decoder accepts unpadded input.
            val payload = Base64.getUrlDecoder().decode(parts[1]);
            return new ObjectMapper().readValue(payload, new TypeReference<Map<String, Object>>() {});
        } catch (IOException | IllegalArgumentException e) {
            return Map.of();
        }
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
