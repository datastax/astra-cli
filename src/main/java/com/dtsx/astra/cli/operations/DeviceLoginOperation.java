package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.operations.LoginOperation.LoginResult;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * Implements the RFC 8628 Device Authorization Grant flow for headless / SSH logins.
 *
 * <ol>
 *   <li>POST /v2/oauth/device/code — obtain device_code, user_code, verification_uri, interval</li>
 *   <li>Print the user_code and verification_uri to the terminal</li>
 *   <li>Poll POST /v2/oauth/token until approved, expired, or timed out</li>
 *   <li>Save the returned access_token to ~/.astrarc (same as PKCE flow)</li>
 * </ol>
 */
@RequiredArgsConstructor
public class DeviceLoginOperation implements Operation<LoginResult> {

    private static final int    TIMEOUT_SECONDS  = 300;
    private static final int    DEFAULT_INTERVAL = 5;
    private static final String CLIENT_ID        = "astra-cli";
    private static final String DEVICE_GRANT     = "urn:ietf:params:oauth:grant-type:device_code";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final CliContext ctx;
    private final DeviceLoginRequest request;

    public record DeviceLoginRequest(
        Optional<String> tokenBaseUrl,
        Optional<ProfileName> profileName,
        boolean setAsDefault
    ) {}

    // LoginResult sealed types are reused from LoginOperation

    @Override
    public LoginResult execute() {
        val baseUrl = request.tokenBaseUrl()
            .or(() -> Optional.ofNullable(System.getenv("ASTRA_LOGIN_URL")))
            .map(DeviceLoginOperation::tokenBaseFromLoginBase)
            .orElse("https://astra.datastax.com");

        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        // ── Step 1: request device + user codes ──────────────────────────────
        val deviceCodeResp = postForm(client,
            baseUrl + "/v2/oauth/device/code",
            "client_id=" + urlencode(CLIENT_ID));

        if (deviceCodeResp.isEmpty()) {
            return new LoginOperation.LoginError("No response from device authorization endpoint.");
        }

        JsonNode deviceJson;
        try {
            deviceJson = MAPPER.readTree(deviceCodeResp.get());
        } catch (IOException e) {
            return new LoginOperation.LoginError("Could not parse device code response: " + e.getMessage());
        }

        if (deviceJson.has("error")) {
            return new LoginOperation.LoginError("Device code request failed: " + deviceJson.path("error").asText());
        }

        val deviceCode      = deviceJson.path("device_code").asText();
        val userCode        = deviceJson.path("user_code").asText();
        val verificationUri = deviceJson.path("verification_uri").asText(baseUrl + "/v2/oauth/device/activate");
        val expiresIn       = deviceJson.path("expires_in").asInt(TIMEOUT_SECONDS);
        int intervalSecs    = deviceJson.path("interval").asInt(DEFAULT_INTERVAL);

        // ── Step 2: display instructions ─────────────────────────────────────
        ctx.console().println();
        ctx.console().println("  Open the following URL in any browser:");
        ctx.console().println();
        ctx.console().println("    " + verificationUri);
        ctx.console().println();
        ctx.console().println("  Then enter this code: @|bold,cyan " + userCode + "|@");
        ctx.console().println();
        ctx.console().println("  Waiting for approval (expires in " + expiresIn + "s)...");

        // ── Step 3: poll until approved / expired / timed-out ────────────────
        val deadline = System.currentTimeMillis() + (long) expiresIn * 1000;
        String accessToken = null;

        while (System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(intervalSecs * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new LoginOperation.LoginCancelled();
            }

            val pollResp = postForm(client,
                baseUrl + "/v2/oauth/token",
                "grant_type=" + urlencode(DEVICE_GRANT)
                    + "&device_code=" + urlencode(deviceCode));

            if (pollResp.isEmpty()) {
                continue;
            }

            JsonNode pollJson;
            try {
                pollJson = MAPPER.readTree(pollResp.get());
            } catch (IOException e) {
                continue;
            }

            if (pollJson.has("access_token")) {
                accessToken = pollJson.path("access_token").asText();
                break;
            }

            val error = pollJson.path("error").asText("");
            switch (error) {
                case "authorization_pending" -> {
                    // normal — keep waiting, print a dot as progress indicator
                    ctx.console().print(".");
                }
                case "slow_down" -> {
                    // server asked us to back off
                    intervalSecs += 5;
                    ctx.console().print(".");
                }
                case "expired_token" -> {
                    ctx.console().println();
                    return new LoginOperation.LoginError("The device code expired. Please run 'astra login --device' again.");
                }
                case "access_denied" -> {
                    ctx.console().println();
                    return new LoginOperation.LoginError("Login was denied by the user.");
                }
                default -> {
                    ctx.console().println();
                    return new LoginOperation.LoginError("Unexpected error while polling: " + error);
                }
            }
        }

        ctx.console().println();

        if (accessToken == null) {
            return new LoginOperation.LoginCancelled();
        }

        // ── Step 4: save token ────────────────────────────────────────────────
        val token       = AstraToken.mkUnsafe(accessToken);
        val profileName = request.profileName().orElse(ProfileName.mkUnsafe("default"));

        val config = AstraConfig.readAstraConfigFile(ctx, null, true);
        config.modify(modCtx -> {
            modCtx.deleteProfile(profileName);
            modCtx.createProfile(profileName, token, AstraEnvironment.PROD);
            if (request.setAsDefault()) {
                modCtx.setDefault(profileName);
            }
        });

        return new LoginOperation.LoginSuccess(profileName, request.setAsDefault());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * ASTRA_LOGIN_URL / --login-url points at the UI login page base (e.g.
     * "http://localhost:8082/v1/login"). Strip the path so we get just the
     * origin to build token/device endpoints from.
     */
    private static String tokenBaseFromLoginBase(String loginUrl) {
        try {
            val uri = URI.create(loginUrl);
            return uri.getScheme() + "://" + uri.getHost()
                + (uri.getPort() != -1 ? ":" + uri.getPort() : "");
        } catch (Exception e) {
            return loginUrl;
        }
    }

    private static Optional<String> postForm(HttpClient client, String url, String body) {
        try {
            val req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            val resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            return Optional.of(resp.body());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private static String urlencode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
