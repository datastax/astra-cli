package com.dtsx.astra.cli.unit.commands;

import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.credentials.CredentialStorageFactory;
import com.dtsx.astra.cli.core.credentials.FileFallbackStorage;
import com.dtsx.astra.cli.operations.LoginOperation;
import com.dtsx.astra.cli.operations.LoginOperation.LoginRequest;
import com.dtsx.astra.cli.operations.LoginOperation.LoginResult;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
            val mockPortHolder = new java.util.concurrent.atomic.AtomicInteger();

            mockTokenServer.createContext("/v2/oauth/token", exchange -> {
                val bodyBytes = exchange.getRequestBody().readAllBytes();
                val body = new String(bodyBytes, StandardCharsets.UTF_8);
                val params = parseForm(body);

                assertThat(params).containsEntry("grant_type", "authorization_code");
                assertThat(params).containsEntry("code", fakeCode);
                assertThat(params).containsEntry("client_id", "astra-cli");
                assertThat(params).containsKey("code_verifier");
                assertThat(params).containsKey("redirect_uri");

                assertThat(params).containsEntry("resource", "http://localhost:" + mockPortHolder.get() + "/v1/mcp");

                receivedVerifier.set(params.get("code_verifier"));

                val resp = ("{\"access_token\":\"" + fakeJwt + "\",\"token_type\":\"Bearer\",\"expires_in\":3600}").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
                tokenEndpointCalled.countDown();
            });
            mockTokenServer.start();
            int mockPort = mockTokenServer.getAddress().getPort();
            mockPortHolder.set(mockPort);

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
                assertThat(queryParam(authorizeUrl, "resource")).isEqualTo("http://localhost:" + mockPort + "/v1/mcp");

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

        /** Shared userinfo JSON for org 'test-org-uuid' with one role 'test-role-uuid'. */
        private static final String USERINFO_RESP = """
            {"orgs":[{"id":"test-org-uuid","name":"Test Org","roles":[{"id":"test-role-uuid","name":"Database Administrator"}]}]}
            """;

        @BeforeEach
        void useFileStorage() {
            CredentialStorageFactory.setOverrideForTest(new FileFallbackStorage());
        }

        @AfterEach
        void clearStorageOverride() {
            CredentialStorageFactory.clearOverrideForTest();
        }

        /** Registers the three mock endpoints (token, userinfo, client-token) on {@code mockServer}. */
        private static void registerMockEndpoints(HttpServer mockServer, String fakeJwt) {
            mockServer.createContext("/v2/oauth/token", exchange -> {
                val resp = ("{\"access_token\":\"" + fakeJwt + "\",\"token_type\":\"Bearer\",\"expires_in\":3600}").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
            });

            mockServer.createContext("/v2/oauth/userinfo", exchange -> {
                val resp = USERINFO_RESP.strip().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
            });

            mockServer.createContext("/v2/oauth/client-token", exchange -> {
                val resp = "{\"token\":\"AstraCS:testclient:abc123\",\"clientId\":\"testclient\",\"orgId\":\"test-org-uuid\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
            });
        }

        @Test
        @SneakyThrows
        void provisions_cs_token_and_writes_to_profile() {
            val fakeJwt = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.sig";
            val fakeCode = "auth-code-cs";

            val mockServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            registerMockEndpoints(mockServer, fakeJwt);
            mockServer.start();
            int mockPort = mockServer.getAddress().getPort();

            try {
                // Single org + single role → auto-selected; no interactive prompt needed.
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

        @Test
        @SneakyThrows
        void provisions_cs_token_when_org_and_role_supplied_as_flags() {
            val fakeJwt = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.sig";
            val fakeCode = "auth-code-flags";

            val mockServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            registerMockEndpoints(mockServer, fakeJwt);
            mockServer.start();
            int mockPort = mockServer.getAddress().getPort();

            try {
                // Org + role supplied via flags — no prompt needed even if multiple existed.
                val future = startOperation(new LoginRequest(
                    Optional.of("http://localhost:" + mockPort + "/v1/login"),
                    Optional.of(ProfileName.mkUnsafe("flag-profile")),
                    true,
                    Optional.of("Test Org"),            // --org by display name
                    Optional.of("Database Administrator") // --role by display name
                ));

                val authorizeUrl = waitForAuthorizeUrl();
                assertThat(authorizeUrl).isNotNull();
                val state = queryParam(authorizeUrl, "state");
                val callbackPort = portFrom(authorizeUrl);

                sendCallback(callbackPort, fakeCode, state);

                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result).isInstanceOf(LoginOperation.LoginSuccess.class);

                val config = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
                val section = config.lookupSection("flag-profile");
                assertThat(section).as("flag-profile section must exist").isPresent();
                val csToken = section.get().lookupKey("ASTRA_CS_TOKEN");
                assertThat(csToken).as("ASTRA_CS_TOKEN must be written with flags").isPresent();
                assertThat(csToken.get()).isEqualTo("AstraCS:testclient:abc123");
            } finally {
                mockServer.stop(0);
            }
        }

        @Test
        @SneakyThrows
        void skips_cs_provisioning_when_userinfo_endpoint_unavailable() {
            val fakeJwt = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.sig";
            val fakeCode = "auth-code-noinfo";

            val mockServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

            // Token endpoint only — no /v2/oauth/userinfo → fetchOrgs returns null → skip
            mockServer.createContext("/v2/oauth/token", exchange -> {
                val resp = ("{\"access_token\":\"" + fakeJwt + "\",\"token_type\":\"Bearer\",\"expires_in\":3600}").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
            });

            mockServer.start();
            int mockPort = mockServer.getAddress().getPort();

            try {
                val future = startOperation(new LoginRequest(
                    Optional.of("http://localhost:" + mockPort + "/v1/login"),
                    Optional.of(ProfileName.mkUnsafe("nocs-profile")),
                    false
                ));

                val authorizeUrl = waitForAuthorizeUrl();
                assertThat(authorizeUrl).isNotNull();
                val state = queryParam(authorizeUrl, "state");
                val callbackPort = portFrom(authorizeUrl);

                sendCallback(callbackPort, fakeCode, state);

                // Login must still succeed — CS provisioning is non-fatal
                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result).isInstanceOf(LoginOperation.LoginSuccess.class);

                // No ASTRA_CS_TOKEN expected
                val config = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
                val section = config.lookupSection("nocs-profile");
                assertThat(section).as("nocs-profile section must exist").isPresent();
                assertThat(section.get().lookupKey("ASTRA_CS_TOKEN"))
                    .as("ASTRA_CS_TOKEN must be absent when userinfo is unavailable")
                    .isEmpty();
            } finally {
                mockServer.stop(0);
            }
        }

        @Test
        @SneakyThrows
        void skips_cs_provisioning_when_org_flag_does_not_match() {
            val fakeJwt = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.sig";
            val fakeCode = "auth-code-badorg";

            val mockServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            registerMockEndpoints(mockServer, fakeJwt);
            mockServer.start();
            int mockPort = mockServer.getAddress().getPort();

            try {
                val future = startOperation(new LoginRequest(
                    Optional.of("http://localhost:" + mockPort + "/v1/login"),
                    Optional.of(ProfileName.mkUnsafe("badorg-profile")),
                    false,
                    Optional.of("Nonexistent Org"),   // --org that matches nothing
                    Optional.empty()
                ));

                val authorizeUrl = waitForAuthorizeUrl();
                assertThat(authorizeUrl).isNotNull();
                val state = queryParam(authorizeUrl, "state");
                val callbackPort = portFrom(authorizeUrl);

                sendCallback(callbackPort, fakeCode, state);

                // Login still succeeds — CS provisioning is non-fatal
                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result).isInstanceOf(LoginOperation.LoginSuccess.class);

                val config = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
                val section = config.lookupSection("badorg-profile");
                assertThat(section).isPresent();
                assertThat(section.get().lookupKey("ASTRA_CS_TOKEN"))
                    .as("ASTRA_CS_TOKEN must be absent when --org flag is unrecognised")
                    .isEmpty();
            } finally {
                mockServer.stop(0);
            }
        }

        @Test
        @SneakyThrows
        void provisions_cs_token_to_keychain_when_keychain_flag_is_true() {
            val fakeJwt = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.sig";
            val fakeCode = "auth-code-kc";

            class MockKeychain implements com.dtsx.astra.cli.core.credentials.CredentialStorageManager {
                final Map<String, String> storage = new ConcurrentHashMap<>();
                @Override public String name() { return "Mock Keychain"; }
                @Override public boolean store(String service, String account, String secret) {
                    storage.put(service + "/" + account, secret);
                    return true;
                }
                @Override public String retrieve(String service, String account) {
                    return storage.get(service + "/" + account);
                }
                @Override public boolean delete(String service, String account) {
                    return storage.remove(service + "/" + account) != null;
                }
            }

            val mockKeychain = new MockKeychain();
            CredentialStorageFactory.setOverrideForTest(mockKeychain);

            val mockServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            registerMockEndpoints(mockServer, fakeJwt);
            mockServer.start();
            int mockPort = mockServer.getAddress().getPort();

            try {
                val future = startOperation(new LoginRequest(
                    Optional.of("http://localhost:" + mockPort + "/v1/login"),
                    Optional.of(ProfileName.mkUnsafe("kc-profile")),
                    true,
                    Optional.of("Test Org"),
                    Optional.of("Database Administrator"),
                    Optional.empty(),
                    Optional.of(true) // explicit keychain storage
                ));

                val authorizeUrl = waitForAuthorizeUrl();
                assertThat(authorizeUrl).isNotNull();
                val state = queryParam(authorizeUrl, "state");
                val callbackPort = portFrom(authorizeUrl);

                sendCallback(callbackPort, fakeCode, state);

                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result).isInstanceOf(LoginOperation.LoginSuccess.class);

                val config = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
                val section = config.lookupSection("kc-profile");
                assertThat(section).isPresent();
                assertThat(section.get().lookupKey("ASTRA_CS_STORAGE")).hasValue("keychain");
                assertThat(section.get().lookupKey("ASTRA_CS_TOKEN")).isEmpty();

                val storedSecret = mockKeychain.retrieve(
                    CredentialStorageFactory.SERVICE_NAME,
                    "kc-profile.ASTRA_CS_TOKEN"
                );
                assertThat(storedSecret).isEqualTo("AstraCS:testclient:abc123");
            } finally {
                mockServer.stop(0);
            }
        }

        @Test
        @SneakyThrows
        void provisions_cs_token_to_file_when_no_keychain_flag_is_true() {
            val fakeJwt = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.sig";
            val fakeCode = "auth-code-nokc";

            class MockKeychain implements com.dtsx.astra.cli.core.credentials.CredentialStorageManager {
                final Map<String, String> storage = new ConcurrentHashMap<>();
                @Override public String name() { return "Mock Keychain"; }
                @Override public boolean store(String service, String account, String secret) {
                    storage.put(service + "/" + account, secret);
                    return true;
                }
                @Override public String retrieve(String service, String account) {
                    return storage.get(service + "/" + account);
                }
                @Override public boolean delete(String service, String account) {
                    return storage.remove(service + "/" + account) != null;
                }
            }

            val mockKeychain = new MockKeychain();
            CredentialStorageFactory.setOverrideForTest(mockKeychain);

            val mockServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            registerMockEndpoints(mockServer, fakeJwt);
            mockServer.start();
            int mockPort = mockServer.getAddress().getPort();

            try {
                val future = startOperation(new LoginRequest(
                    Optional.of("http://localhost:" + mockPort + "/v1/login"),
                    Optional.of(ProfileName.mkUnsafe("file-profile")),
                    true,
                    Optional.of("Test Org"),
                    Optional.of("Database Administrator"),
                    Optional.empty(),
                    Optional.of(false) // explicit file storage
                ));

                val authorizeUrl = waitForAuthorizeUrl();
                assertThat(authorizeUrl).isNotNull();
                val state = queryParam(authorizeUrl, "state");
                val callbackPort = portFrom(authorizeUrl);

                sendCallback(callbackPort, fakeCode, state);

                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result).isInstanceOf(LoginOperation.LoginSuccess.class);

                val config = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
                val section = config.lookupSection("file-profile");
                assertThat(section).isPresent();
                assertThat(section.get().lookupKey("ASTRA_CS_STORAGE")).isEmpty();
                assertThat(section.get().lookupKey("ASTRA_CS_TOKEN")).hasValue("AstraCS:testclient:abc123");

                val storedSecret = mockKeychain.retrieve(
                    CredentialStorageFactory.SERVICE_NAME,
                    "file-profile.ASTRA_CS_TOKEN"
                );
                assertThat(storedSecret).isNull();
            } finally {
                mockServer.stop(0);
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // Test – org/role selection made in the browser (carried in the token response)
    // -----------------------------------------------------------------------------------------

    @Nested
    class browser_selection {

        private static final String FAKE_JWT = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.sig";

        private final java.util.concurrent.atomic.AtomicInteger userinfoCalls = new java.util.concurrent.atomic.AtomicInteger();
        private final java.util.concurrent.atomic.AtomicReference<String> clientTokenBody = new java.util.concurrent.atomic.AtomicReference<>();

        @BeforeEach
        void useFileStorage() {
            CredentialStorageFactory.setOverrideForTest(new FileFallbackStorage());
        }

        @AfterEach
        void clearStorageOverride() {
            CredentialStorageFactory.clearOverrideForTest();
        }

        /** Mock session-service whose token response body is {@code tokenResponseJson}; records userinfo calls and the client-token body. */
        @SneakyThrows
        private HttpServer startMockServer(String tokenResponseJson) {
            val mockServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

            mockServer.createContext("/v2/oauth/token", exchange -> {
                val resp = tokenResponseJson.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
            });

            mockServer.createContext("/v2/oauth/userinfo", exchange -> {
                userinfoCalls.incrementAndGet();
                val resp = "{\"orgs\":[{\"id\":\"userinfo-org\",\"name\":\"Userinfo Org\",\"roles\":[{\"id\":\"userinfo-role\",\"name\":\"Userinfo Role\"}]}]}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
            });

            mockServer.createContext("/v2/oauth/client-token", exchange -> {
                clientTokenBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                val resp = "{\"token\":\"AstraCS:testclient:abc123\",\"clientId\":\"testclient\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
            });

            mockServer.start();
            return mockServer;
        }

        /** Starts the login, drives a successful code callback and returns the result. */
        @SneakyThrows
        private LoginResult loginWithCodeCallback(HttpServer mockServer, String profile, Optional<String> orgFlag, Optional<String> roleFlag) {
            val future = startOperation(new LoginRequest(
                Optional.of("http://localhost:" + mockServer.getAddress().getPort() + "/v1/login"),
                Optional.of(ProfileName.mkUnsafe(profile)),
                false,
                orgFlag,
                roleFlag
            ));

            val authorizeUrl = waitForAuthorizeUrl();
            assertThat(authorizeUrl).isNotNull();
            sendCallback(portFrom(authorizeUrl), "fake-code", queryParam(authorizeUrl, "state"));

            return future.get(10, TimeUnit.SECONDS);
        }

        @SneakyThrows
        private void assertStoredMeta(String profile, String orgId, String orgName, String roleId, String roleName) {
            val config = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
            val section = config.lookupSection(profile);
            assertThat(section).as(profile + " section must exist").isPresent();
            assertThat(section.get().lookupKey("ASTRA_CS_TOKEN")).hasValue("AstraCS:testclient:abc123");
            assertThat(section.get().lookupKey(AstraConfig.CS_ORG_ID_KEY)).hasValue(orgId);
            assertThat(section.get().lookupKey(AstraConfig.CS_ORG_NAME_KEY)).hasValue(orgName);
            assertThat(section.get().lookupKey(AstraConfig.CS_ROLE_ID_KEY)).hasValue(roleId);
            assertThat(section.get().lookupKey(AstraConfig.CS_ROLE_NAME_KEY)).hasValue(roleName);
        }

        @Test
        @SneakyThrows
        void approve_uses_selection_from_token_response_without_calling_userinfo() {
            val mockServer = startMockServer("{\"access_token\":\"" + FAKE_JWT + "\",\"token_type\":\"Bearer\",\"expires_in\":3600,"
                + "\"org_id\":\"sel-org-uuid\",\"role_id\":\"sel-role-uuid\",\"org_name\":\"Selected Org\",\"role_name\":\"Selected Role\"}");

            try {
                val result = loginWithCodeCallback(mockServer, "sel-profile", Optional.empty(), Optional.empty());
                assertThat(result).isInstanceOf(LoginOperation.LoginSuccess.class);

                assertThat(userinfoCalls.get()).as("userinfo must not be called when the token response carries a selection").isZero();

                val body = new com.fasterxml.jackson.databind.ObjectMapper().readTree(clientTokenBody.get());
                assertThat(body.path("orgId").asText()).isEqualTo("sel-org-uuid");
                assertThat(body.path("roles")).hasSize(1);
                assertThat(body.path("roles").get(0).asText()).isEqualTo("sel-role-uuid");

                assertStoredMeta("sel-profile", "sel-org-uuid", "Selected Org", "sel-role-uuid", "Selected Role");
            } finally {
                mockServer.stop(0);
            }
        }

        @Test
        @SneakyThrows
        void selection_without_names_stores_ids_as_names() {
            val mockServer = startMockServer("{\"access_token\":\"" + FAKE_JWT + "\",\"token_type\":\"Bearer\",\"expires_in\":3600,"
                + "\"org_id\":\"sel-org-uuid\",\"role_id\":\"sel-role-uuid\"}");

            try {
                val result = loginWithCodeCallback(mockServer, "noname-profile", Optional.empty(), Optional.empty());
                assertThat(result).isInstanceOf(LoginOperation.LoginSuccess.class);

                assertThat(userinfoCalls.get()).isZero();
                assertStoredMeta("noname-profile", "sel-org-uuid", "sel-org-uuid", "sel-role-uuid", "sel-role-uuid");
            } finally {
                mockServer.stop(0);
            }
        }

        @Test
        @SneakyThrows
        void deny_callback_returns_cancellation_message_and_writes_no_profile() {
            val mockServer = startMockServer("{\"access_token\":\"" + FAKE_JWT + "\"}");

            try {
                val future = startOperation(new LoginRequest(
                    Optional.of("http://localhost:" + mockServer.getAddress().getPort() + "/v1/login"),
                    Optional.of(ProfileName.mkUnsafe("denied-profile")),
                    true
                ));

                val authorizeUrl = waitForAuthorizeUrl();
                assertThat(authorizeUrl).isNotNull();
                sendErrorCallback(portFrom(authorizeUrl), "access_denied", "User cancelled the selection");

                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result)
                    .asInstanceOf(InstanceOfAssertFactories.type(LoginOperation.LoginError.class))
                    .satisfies(e -> {
                        assertThat(e.message()).containsIgnoringCase("cancelled");
                        assertThat(e.message()).contains("User cancelled the selection");
                        assertThat(e.message()).contains("No profile was saved");
                    });

                assertThat(userinfoCalls.get()).isZero();
                assertThat(clientTokenBody.get()).isNull();
                assertThat(java.nio.file.Files.exists(AstraConfig.resolveDefaultAstraConfigFile(ctx.get())))
                    .as("no config file (and so no profile) must be written on deny").isFalse();
            } finally {
                mockServer.stop(0);
            }
        }

        @Test
        @SneakyThrows
        void conflicting_flags_print_a_warning_and_the_selection_still_wins() {
            val mockServer = startMockServer("{\"access_token\":\"" + FAKE_JWT + "\",\"token_type\":\"Bearer\",\"expires_in\":3600,"
                + "\"org_id\":\"sel-org-uuid\",\"role_id\":\"sel-role-uuid\",\"org_name\":\"Selected Org\",\"role_name\":\"Selected Role\"}");

            try {
                val result = loginWithCodeCallback(mockServer, "conflict-profile", Optional.of("Other Org"), Optional.of("Other Role"));
                assertThat(result).isInstanceOf(LoginOperation.LoginSuccess.class);

                val output = ctx.rawOutput().stream().map(TestCliContext.OutputLine::unwrap).toList();
                assertThat(output).anyMatch(l -> l.contains("--org 'Other Org' is ignored"));
                assertThat(output).anyMatch(l -> l.contains("--role 'Other Role' is ignored"));

                assertThat(userinfoCalls.get()).isZero();
                assertStoredMeta("conflict-profile", "sel-org-uuid", "Selected Org", "sel-role-uuid", "Selected Role");
            } finally {
                mockServer.stop(0);
            }
        }

        @Test
        @SneakyThrows
        void matching_flags_print_no_warning() {
            val mockServer = startMockServer("{\"access_token\":\"" + FAKE_JWT + "\",\"token_type\":\"Bearer\",\"expires_in\":3600,"
                + "\"org_id\":\"sel-org-uuid\",\"role_id\":\"sel-role-uuid\",\"org_name\":\"Selected Org\",\"role_name\":\"Selected Role\"}");

            try {
                val result = loginWithCodeCallback(mockServer, "match-profile", Optional.of("selected org"), Optional.of("SEL-ROLE-UUID"));
                assertThat(result).isInstanceOf(LoginOperation.LoginSuccess.class);

                val output = ctx.rawOutput().stream().map(TestCliContext.OutputLine::unwrap).toList();
                assertThat(output).noneMatch(l -> l.contains("is ignored"));
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
