package com.dtsx.astra.cli.unit.commands;

import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.operations.LoginOperation;
import com.dtsx.astra.cli.operations.LoginOperation.LoginRequest;
import com.dtsx.astra.cli.operations.LoginOperation.LoginResult;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the RFC 7636 PKCE login flow in LoginOperation.
 *
 * Each test starts the operation on a background thread, drives the loopback callback itself
 * (simulating what the browser would do after the AS redirects), and asserts the result.
 */
public class LoginCmdTest {

    @UseTestCtx(fs = "jimfs")
    private TestCliContext ctx;

    // -----------------------------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------------------------

    private static final Pattern PORT_RE = Pattern.compile("localhost:(\\d+)/callback");

    /**
     * Starts a {@link LoginOperation} on a background thread and returns its Future.
     * The test drives the callback and mock token endpoint itself.
     */
    private Future<LoginResult> startOperation(LoginRequest request) {
        val exec = Executors.newSingleThreadExecutor();
        return exec.submit(() -> new LoginOperation(ctx.get(), request).execute());
    }

    /**
     * Polls stdout until a line containing "localhost:" appears, then returns that line
     * (URL-decoded so encoded redirect_uri params are readable).
     */
    @SneakyThrows
    private String waitForAuthorizeUrl() {
        for (int i = 0; i < 200; i++) {
            Thread.sleep(30);
            val line = ctx.rawOutput().stream()
                .map(TestCliContext.OutputLine::unwrap)
                .map(s -> URLDecoder.decode(s, StandardCharsets.UTF_8))
                .filter(s -> s.contains("localhost:"))
                .findFirst().orElse(null);
            if (line != null) return line;
        }
        return null;
    }

    /** Parse {@code localhost:PORT/callback} from the printed authorize URL line. */
    private static int portFrom(String authorizeUrl) {
        val m = PORT_RE.matcher(authorizeUrl);
        assertThat(m.find()).as("port must be present in authorize URL").isTrue();
        return Integer.parseInt(m.group(1));
    }

    /**
     * Extract the value of a query parameter from the printed authorize URL.
     * The URL is already URL-decoded by {@link #waitForAuthorizeUrl()}, so we match literally.
     */
    private static String queryParam(String url, String name) {
        val pattern = Pattern.compile("[?&]" + Pattern.quote(name) + "=([^&]+)");
        val m = pattern.matcher(url);
        assertThat(m.find()).as("'" + name + "' must be present in authorize URL").isTrue();
        return m.group(1);
    }

    /** Send a GET to the loopback callback simulating a browser redirect from the AS. */
    @SneakyThrows
    private static int sendCallback(int port, String code, String state) {
        val raw = "http://localhost:" + port + "/callback"
            + "?code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
            + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
        val conn = (HttpURLConnection) new URL(raw).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5_000);
        conn.setReadTimeout(5_000);
        int rc = conn.getResponseCode();
        conn.disconnect();
        return rc;
    }

    /** Send a callback with an error response (AS rejected the request). */
    @SneakyThrows
    private static int sendErrorCallback(int port, String error, String description) {
        val raw = "http://localhost:" + port + "/callback"
            + "?error=" + URLEncoder.encode(error, StandardCharsets.UTF_8)
            + "&error_description=" + URLEncoder.encode(description, StandardCharsets.UTF_8);
        val conn = (HttpURLConnection) new URL(raw).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5_000);
        conn.setReadTimeout(5_000);
        int rc = conn.getResponseCode();
        conn.disconnect();
        return rc;
    }

    /**
     * Verify the PKCE S256 contract: SHA-256 of the verifier must equal the challenge.
     * Both verifier and challenge come from the *printed* authorize URL so the test
     * confirms the operation advertised the right values.
     */
    private static void assertChallengeMathChecksOut(String codeVerifier, String codeChallenge) throws Exception {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        val expected = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        assertThat(codeChallenge).as("code_challenge must equal BASE64URL(SHA256(code_verifier))")
            .isEqualTo(expected);
    }

    // -----------------------------------------------------------------------------------------
    // Test – loopback server and happy path
    // -----------------------------------------------------------------------------------------

    @Nested
    class loopback_server {

        @Test
        @SneakyThrows
        void receives_code_exchanges_for_token_and_writes_profile() {
            val fakeJwt = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyMSJ9.sig";
            val fakeCode = "auth-code-abc123";

            // Spin up a mock POST /v2/oauth/token endpoint
            val mockTokenServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            val tokenEndpointCalled = new CountDownLatch(1);
            val receivedVerifier = new java.util.concurrent.atomic.AtomicReference<String>();

            mockTokenServer.createContext("/v2/oauth/token", exchange -> {
                val bodyBytes = exchange.getRequestBody().readAllBytes();
                val body = new String(bodyBytes, StandardCharsets.UTF_8);
                val params = parseForm(body);

                assertThat(params).containsEntry("grant_type", "authorization_code");
                assertThat(params).containsEntry("code", fakeCode);
                assertThat(params).containsEntry("client_id", "astra-cli");
                assertThat(params).containsKey("code_verifier");
                assertThat(params).containsKey("redirect_uri");

                receivedVerifier.set(params.get("code_verifier"));

                val resp = ("{\"access_token\":\"" + fakeJwt + "\",\"token_type\":\"Bearer\",\"expires_in\":3600}").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
                tokenEndpointCalled.countDown();
            });
            mockTokenServer.start();
            int mockPort = mockTokenServer.getAddress().getPort();

            try {
                // Use http://localhost:<mockPort>/v1/login so sessionBase = http://localhost:<mockPort>
                val loginBaseUrl = "http://localhost:" + mockPort + "/v1/login";
                val request = new LoginRequest(
                    Optional.of(loginBaseUrl),
                    Optional.of(ProfileName.mkUnsafe("pkce-profile")),
                    true
                );
                val future = startOperation(request);

                val authorizeUrl = waitForAuthorizeUrl();
                assertThat(authorizeUrl).as("authorize URL must be printed").isNotNull();

                // Verify PKCE params are present
                assertThat(queryParam(authorizeUrl, "response_type")).isEqualTo("code");
                assertThat(queryParam(authorizeUrl, "client_id")).isEqualTo("astra-cli");
                assertThat(queryParam(authorizeUrl, "code_challenge_method")).isEqualTo("S256");

                val printedChallenge = queryParam(authorizeUrl, "code_challenge");
                val state = queryParam(authorizeUrl, "state");
                val callbackPort = portFrom(authorizeUrl);

                // Send the browser callback with code + matching state
                val httpStatus = sendCallback(callbackPort, fakeCode, state);
                assertThat(httpStatus).isEqualTo(200);

                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result).isInstanceOf(LoginOperation.LoginSuccess.class);

                // Verify the verifier/challenge math
                assertThat(tokenEndpointCalled.await(3, TimeUnit.SECONDS)).isTrue();
                assertChallengeMathChecksOut(receivedVerifier.get(), printedChallenge);

                // Profile must be persisted
                val config = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
                val saved = config.lookupProfile(ProfileName.mkUnsafe("pkce-profile"));
                assertThat(saved).isPresent();
                assertThat(saved.get().token().unsafeUnwrap()).isEqualTo(fakeJwt);
            } finally {
                mockTokenServer.stop(0);
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // Test – state mismatch
    // -----------------------------------------------------------------------------------------

    @Nested
    class state_validation {

        @Test
        @SneakyThrows
        void rejects_callback_with_wrong_state() {
            val mockTokenServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            mockTokenServer.createContext("/v2/oauth/token", exchange -> {
                // should never be reached
                exchange.sendResponseHeaders(500, 0);
                exchange.getResponseBody().close();
            });
            mockTokenServer.start();
            int mockPort = mockTokenServer.getAddress().getPort();

            try {
                val loginBaseUrl = "http://localhost:" + mockPort + "/v1/login";
                val future = startOperation(new LoginRequest(
                    Optional.of(loginBaseUrl),
                    Optional.of(ProfileName.mkUnsafe("state-profile")),
                    false
                ));

                val authorizeUrl = waitForAuthorizeUrl();
                assertThat(authorizeUrl).isNotNull();
                val callbackPort = portFrom(authorizeUrl);

                // Send a callback with a different state value
                sendCallback(callbackPort, "somecode", "WRONG-STATE-VALUE");

                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(LoginOperation.LoginError.class))
                    .satisfies(e -> assertThat(e.message()).containsIgnoringCase("state"));
            } finally {
                mockTokenServer.stop(0);
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // Test – error callback from AS
    // -----------------------------------------------------------------------------------------

    @Nested
    class error_callback {

        @Test
        @SneakyThrows
        void returns_login_error_when_as_sends_error_response() {
            val mockTokenServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            mockTokenServer.createContext("/v2/oauth/token", exchange -> {
                exchange.sendResponseHeaders(500, 0);
                exchange.getResponseBody().close();
            });
            mockTokenServer.start();
            int mockPort = mockTokenServer.getAddress().getPort();

            try {
                val loginBaseUrl = "http://localhost:" + mockPort + "/v1/login";
                val future = startOperation(new LoginRequest(
                    Optional.of(loginBaseUrl),
                    Optional.of(ProfileName.mkUnsafe("err-profile")),
                    false
                ));

                val authorizeUrl = waitForAuthorizeUrl();
                assertThat(authorizeUrl).isNotNull();
                val callbackPort = portFrom(authorizeUrl);

                sendErrorCallback(callbackPort, "access_denied", "The user denied the request.");

                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(LoginOperation.LoginError.class))
                    .satisfies(e -> {
                        assertThat(e.message()).containsIgnoringCase("access_denied");
                        assertThat(e.message()).contains("user denied");
                    });
            } finally {
                mockTokenServer.stop(0);
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // Test – token endpoint returns error
    // -----------------------------------------------------------------------------------------

    @Nested
    class token_endpoint_error {

        @Test
        @SneakyThrows
        void returns_login_error_when_token_exchange_fails() {
            val mockTokenServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            mockTokenServer.createContext("/v2/oauth/token", exchange -> {
                val resp = "{\"error\":\"invalid_grant\",\"error_description\":\"Code not found.\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
            });
            mockTokenServer.start();
            int mockPort = mockTokenServer.getAddress().getPort();

            try {
                val loginBaseUrl = "http://localhost:" + mockPort + "/v1/login";
                val future = startOperation(new LoginRequest(
                    Optional.of(loginBaseUrl),
                    Optional.of(ProfileName.mkUnsafe("tokenerr-profile")),
                    false
                ));

                val authorizeUrl = waitForAuthorizeUrl();
                assertThat(authorizeUrl).isNotNull();
                val state = queryParam(authorizeUrl, "state");
                val callbackPort = portFrom(authorizeUrl);

                sendCallback(callbackPort, "fake-code", state);

                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(LoginOperation.LoginError.class))
                    .satisfies(e -> {
                        assertThat(e.message()).containsIgnoringCase("invalid_grant");
                        assertThat(e.message()).contains("Code not found");
                    });
            } finally {
                mockTokenServer.stop(0);
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // Test – timeout / no callback
    // -----------------------------------------------------------------------------------------

    @Nested
    class timeout {

        @Test
        @SneakyThrows
        void returns_error_when_callback_arrives_with_no_code_and_no_state() {
            // We can't wait 120 s; instead simulate a callback with no useful params so the
            // operation unblocks and fails gracefully.
            val mockTokenServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            mockTokenServer.createContext("/v2/oauth/token", exchange -> {
                exchange.sendResponseHeaders(500, 0);
                exchange.getResponseBody().close();
            });
            mockTokenServer.start();
            int mockPort = mockTokenServer.getAddress().getPort();

            try {
                val future = startOperation(new LoginRequest(
                    Optional.of("http://localhost:" + mockPort + "/v1/login"),
                    Optional.of(ProfileName.mkUnsafe("timeout-profile")),
                    false
                ));

                val authorizeUrl = waitForAuthorizeUrl();
                assertThat(authorizeUrl).isNotNull();
                val callbackPort = portFrom(authorizeUrl);

                // Send callback with no code and a wrong state → latch released, then error path
                sendCallback(callbackPort, "", "WRONG-STATE");

                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result).isInstanceOf(LoginOperation.LoginError.class);
            } finally {
                mockTokenServer.stop(0);
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // Test – profile_writing
    // -----------------------------------------------------------------------------------------

    @Nested
    class profile_writing {

        @Test
        @SneakyThrows
        void creates_profile_without_default_when_flag_is_false() {
            val fakeJwt = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyMiJ9.sig2";
            val fakeCode = "auth-code-xyz";

            val mockTokenServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            mockTokenServer.createContext("/v2/oauth/token", exchange -> {
                val resp = ("{\"access_token\":\"" + fakeJwt + "\",\"token_type\":\"Bearer\",\"expires_in\":3600}").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
            });
            mockTokenServer.start();
            int mockPort = mockTokenServer.getAddress().getPort();

            try {
                val future = startOperation(new LoginRequest(
                    Optional.of("http://localhost:" + mockPort + "/v1/login"),
                    Optional.of(ProfileName.mkUnsafe("named-profile")),
                    false
                ));

                val authorizeUrl = waitForAuthorizeUrl();
                assertThat(authorizeUrl).isNotNull();
                val state = queryParam(authorizeUrl, "state");
                val callbackPort = portFrom(authorizeUrl);

                sendCallback(callbackPort, fakeCode, state);

                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(LoginOperation.LoginSuccess.class))
                    .satisfies(s -> {
                        assertThat(s.profileName()).isEqualTo(ProfileName.mkUnsafe("named-profile"));
                        assertThat(s.isDefault()).isFalse();
                    });

                val config = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
                assertThat(config.profiles()).hasSize(1);
                assertThat(config.profiles().getFirst().nameOrDefault())
                    .isEqualTo(ProfileName.mkUnsafe("named-profile"));
            } finally {
                mockTokenServer.stop(0);
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // Test – AstraCS provisioning
    // -----------------------------------------------------------------------------------------

    @Nested
    class astra_cs_token_provisioning {

        @Test
        @SneakyThrows
        void provisions_cs_token_and_writes_to_profile() {
            // JWT with org + grp claims
            val payloadJson = "{\"sub\":\"test\",\"org\":\"test-org-uuid\",\"grp\":[\"test-role-uuid\"]}";
            val b64Header  = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"RS256\"}".getBytes(StandardCharsets.UTF_8));
            val b64Payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            val fakeJwt = b64Header + "." + b64Payload + ".fakesig";
            val fakeCode = "auth-code-cs";

            val mockServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

            // Token endpoint
            mockServer.createContext("/v2/oauth/token", exchange -> {
                val resp = ("{\"access_token\":\"" + fakeJwt + "\",\"token_type\":\"Bearer\",\"expires_in\":3600}").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
            });

            // CS-token endpoint
            mockServer.createContext("/v2/oauth/client-token", exchange -> {
                val resp = "{\"token\":\"AstraCS:testclient:abc123\",\"clientId\":\"testclient\",\"orgId\":\"test-org-uuid\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
            });

            mockServer.start();
            int mockPort = mockServer.getAddress().getPort();

            try {
                val future = startOperation(new LoginRequest(
                    Optional.of("http://localhost:" + mockPort + "/v1/login"),
                    Optional.of(ProfileName.mkUnsafe("cs-profile")),
                    true
                ));

                val authorizeUrl = waitForAuthorizeUrl();
                assertThat(authorizeUrl).isNotNull();
                val state = queryParam(authorizeUrl, "state");
                val callbackPort = portFrom(authorizeUrl);

                sendCallback(callbackPort, fakeCode, state);

                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result).isInstanceOf(LoginOperation.LoginSuccess.class);

                val config = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
                val section = config.lookupSection("cs-profile");
                assertThat(section).as("cs-profile section must exist").isPresent();
                val csToken = section.get().lookupKey("ASTRA_CS_TOKEN");
                assertThat(csToken).as("ASTRA_CS_TOKEN must be present").isPresent();
                assertThat(csToken.get()).isEqualTo("AstraCS:testclient:abc123");
            } finally {
                mockServer.stop(0);
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // Utility
    // -----------------------------------------------------------------------------------------

    /** Parse an {@code application/x-www-form-urlencoded} body into a Map. */
    private static Map<String, String> parseForm(String body) {
        val result = new java.util.HashMap<String, String>();
        if (body == null || body.isBlank()) return result;
        for (val pair : body.split("&")) {
            val kv = pair.split("=", 2);
            if (kv.length == 2) {
                result.put(
                    URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                );
            }
        }
        return result;
    }
}
