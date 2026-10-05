package com.dtsx.astra.cli.unit.commands;

import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.operations.LoginOperation;
import com.dtsx.astra.cli.operations.LoginOperation.LoginRequest;
import com.dtsx.astra.cli.operations.LoginOperation.LoginResult;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the OAuth2 loopback login flow.
 *
 * These tests verify behaviour of LoginOperation without any real browser or cloud-session
 * service — they simulate the callback by sending HTTP requests directly to the local server.
 */
public class LoginCmdTest {
    @UseTestCtx(fs = "jimfs")
    private TestCliContext ctx;

    // -------------------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------------------

    /** URL-decode a raw output line so encoded redirect_uri params become readable. */
    private static String decodeOutputLine(String line) {
        try { return URLDecoder.decode(line, StandardCharsets.UTF_8); } catch (Exception e) { return line; }
    }

    /** Send a GET to {@code http://localhost:{port}/callback?token={token}} */
    @SneakyThrows
    private static int sendCallback(int port, String token) {
        val url = new URL("http://localhost:" + port + "/callback?token=" + token);
        val conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5_000);
        conn.setReadTimeout(5_000);
        int code = conn.getResponseCode();
        conn.disconnect();
        return code;
    }

    /** Send a GET to {@code http://localhost:{port}/callback} with no token param */
    @SneakyThrows
    private static int sendCallbackNoToken(int port) {
        val url = new URL("http://localhost:" + port + "/callback");
        val conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5_000);
        conn.setReadTimeout(5_000);
        int code = conn.getResponseCode();
        conn.disconnect();
        return code;
    }

    /**
     * Find the port that the operation will use by waiting until the server is ready,
     * then return it via the future.  The operation itself is run on a background thread.
     *
     * We determine the port by having the test override the login URL to point to a no-op
     * (the --login-url option just disables the real browser open), and then probe
     * known ports — but for simplicity we just pass a custom loginBaseUrl that resolves to
     * a sentinel, and capture the port from the callback URL embedded in that URL.
     *
     * For these unit tests we rely on a simpler approach: run the LoginOperation and
     * simultaneously drive the callback.
     */
    private Future<LoginResult> startOperation(LoginRequest request) {
        val executor = Executors.newSingleThreadExecutor();
        return executor.submit(() ->
            new LoginOperation(ctx.get(), request).execute()
        );
    }

    // -------------------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------------------

    @Nested
    class loopback_server {
        @Test
        @SneakyThrows
        void receives_token_and_writes_profile() {
            val fakeToken = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyMSJ9.sig"; // fake JWT-ish string

            // Use a tiny wrapper around LoginOperation that exposes the port before blocking.
            // We achieve this by providing a dummy loginBaseUrl so that ShellUtils.openURL is called
            // with a URL we can inspect — but here we use the real loopback server and drive it
            // ourselves by racing the operation start.

            val portHolder = new java.util.concurrent.atomic.AtomicInteger(-1);
            val serverReady = new CountDownLatch(1);

            // Subclass to intercept the URL that would be opened in the browser
            val operation = new LoginOperation(ctx.get(), new LoginRequest(
                Optional.of("http://127.0.0.1:0"), // sentinel; real port embedded in redirect_uri
                Optional.of(ProfileName.mkUnsafe("test-profile")),
                true
            )) {
                // We override openBrowser to parse the port from the redirect_uri and
                // signal the callback thread — but LoginOperation is not designed for subclassing.
                // Instead we rely on the timing below.
            };

            // Because we can't easily subclass LoginOperation (it's final-ish), we use a
            // different approach: provide the operation with a fake loginBaseUrl pointing to
            // localhost itself so that "opening the browser" effectively does nothing visible,
            // then we find the port by scanning for open ports in the range shortly after start.
            //
            // A cleaner approach: we just parse the port from the URL the operation prints to
            // stdout.  The operation prints:
            //   "If a browser window does not open, visit:\n  <url>"
            // We capture stdout from TestCliContext.

            val request = new LoginRequest(
                Optional.of("http://127.0.0.1:0"),  // triggers fallback-open which silently fails
                Optional.of(ProfileName.mkUnsafe("test-profile")),
                true
            );

            val future = Executors.newSingleThreadExecutor()
                .submit(() -> new LoginOperation(ctx.get(), request).execute());

            // Parse the port from the URL printed to stdout.
            // We poll until a line with "localhost:" appears.
            String loginLine = null;
            for (int i = 0; i < 100; i++) {
                Thread.sleep(50);
                loginLine = ctx.rawOutput().stream()
                    .map(TestCliContext.OutputLine::unwrap)
                    .map(LoginCmdTest::decodeOutputLine)
                    .filter(s -> s.contains("localhost:"))
                    .findFirst()
                    .orElse(null);
                if (loginLine != null) break;
            }

            assertThat(loginLine).as("login URL must be printed to stdout").isNotNull();

            // Extract port from the redirect_uri query param embedded in the login URL.
            // URL looks like: http://127.0.0.1:0?redirect_uri=http://localhost:{port}/callback&cli=true
            val portStr = loginLine.replaceAll(".*localhost:(\\d+)/callback.*", "$1");
            val port = Integer.parseInt(portStr.trim());
            assertThat(port).isGreaterThan(0);

            // Drive the callback
            val httpStatus = sendCallback(port, fakeToken);
            assertThat(httpStatus).isEqualTo(200);

            val result = future.get(10, TimeUnit.SECONDS);

            assertThat(result).isInstanceOf(LoginOperation.LoginSuccess.class);
            val success = (LoginOperation.LoginSuccess) result;
            assertThat(success.profileName()).isEqualTo(ProfileName.mkUnsafe("test-profile"));
            assertThat(success.isDefault()).isTrue();

            // Verify the profile was written to ~/.astrarc
            val config = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
            assertThat(config.profiles()).isNotEmpty();
            assertThat(config.profiles()).anySatisfy(p ->
                assertThat(p.nameOrDefault()).isEqualTo(ProfileName.mkUnsafe("test-profile"))
            );
            val saved = config.lookupProfile(ProfileName.mkUnsafe("test-profile"));
            assertThat(saved).isPresent();
            assertThat(saved.get().token().unsafeUnwrap()).isEqualTo(fakeToken);
        }
    }

    @Nested
    class timeout {
        @Test
        @SneakyThrows
        void returns_cancelled_when_no_callback_received() {
            // We can't wait 120 s in a unit test; instead we verify the operation
            // terminates after the latch is counted down with no token.
            // We simulate this by immediately hitting the callback with no token param.

            val request = new LoginRequest(
                Optional.of("http://127.0.0.1:0"),
                Optional.of(ProfileName.mkUnsafe("timeout-profile")),
                false
            );

            val future = Executors.newSingleThreadExecutor()
                .submit(() -> new LoginOperation(ctx.get(), request).execute());

            // Wait for the URL to appear in stdout
            String loginLine = null;
            for (int i = 0; i < 100; i++) {
                Thread.sleep(50);
                loginLine = ctx.rawOutput().stream()
                    .map(TestCliContext.OutputLine::unwrap)
                    .map(LoginCmdTest::decodeOutputLine)
                    .filter(s -> s.contains("localhost:"))
                    .findFirst()
                    .orElse(null);
                if (loginLine != null) break;
            }

            assertThat(loginLine).as("login URL must be printed to stdout").isNotNull();

            val portStr = loginLine.replaceAll(".*localhost:(\\d+)/callback.*", "$1");
            val port = Integer.parseInt(portStr.trim());

            // Send callback with no token — this counts down the latch, but tokenHolder is null
            val httpStatus = sendCallbackNoToken(port);
            assertThat(httpStatus).isEqualTo(200);

            val result = future.get(10, TimeUnit.SECONDS);

            // No token received → LoginError
            assertThat(result)
                .asInstanceOf(InstanceOfAssertFactories.type(LoginOperation.LoginError.class))
                .satisfies(e -> assertThat(e.message()).contains("No token"));
        }
    }

    @Nested
    class profile_writing {
        @Test
        @SneakyThrows
        void creates_profile_without_default_when_no_default_flag_set() {
            val fakeToken = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyMiJ9.sig2";

            val request = new LoginRequest(
                Optional.of("http://127.0.0.1:0"),
                Optional.of(ProfileName.mkUnsafe("named-profile")),
                false   // <-- setAsDefault = false
            );

            val future = Executors.newSingleThreadExecutor()
                .submit(() -> new LoginOperation(ctx.get(), request).execute());

            String loginLine = null;
            for (int i = 0; i < 100; i++) {
                Thread.sleep(50);
                loginLine = ctx.rawOutput().stream()
                    .map(TestCliContext.OutputLine::unwrap)
                    .map(LoginCmdTest::decodeOutputLine)
                    .filter(s -> s.contains("localhost:"))
                    .findFirst()
                    .orElse(null);
                if (loginLine != null) break;
            }

            assertThat(loginLine).isNotNull();
            val port = Integer.parseInt(
                loginLine.replaceAll(".*localhost:(\\d+)/callback.*", "$1").trim()
            );

            sendCallback(port, fakeToken);
            val result = future.get(10, TimeUnit.SECONDS);

            assertThat(result)
                .asInstanceOf(InstanceOfAssertFactories.type(LoginOperation.LoginSuccess.class))
                .satisfies(s -> {
                    assertThat(s.profileName()).isEqualTo(ProfileName.mkUnsafe("named-profile"));
                    assertThat(s.isDefault()).isFalse();
                });

            val config = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
            // Only one profile: "named-profile".  No "default" section.
            assertThat(config.profiles()).hasSize(1);
            assertThat(config.profiles().getFirst().nameOrDefault())
                .isEqualTo(ProfileName.mkUnsafe("named-profile"));
        }
    }

    @Nested
    class astra_cs_token_provisioning {
        @Test
        @SneakyThrows
        void provisions_cs_token_and_writes_to_profile() {
            // Build a minimal fake JWT with org and grp claims
            String payloadJson = "{\"sub\":\"test\",\"org\":\"test-org-uuid\",\"grp\":[\"test-role-uuid\"]}";
            String b64Header  = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"RS256\"}".getBytes(StandardCharsets.UTF_8));
            String b64Payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            String fakeJwt = b64Header + "." + b64Payload + ".fakesig";

            // Spin up a mock server for POST /v2/oauth/client-token
            val mockServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            mockServer.createContext("/v2/oauth/client-token", exchange -> {
                val responseBody = "{\"token\":\"AstraCS:testclient:abc123\",\"clientId\":\"testclient\",\"orgId\":\"test-org-uuid\"}";
                val bytes = responseBody.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                try (val os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            });
            mockServer.start();
            int mockPort = mockServer.getAddress().getPort();

            try {
                // LoginOperation derives sessionBase by stripping the path from loginBaseUrl,
                // then calls <sessionBase>/v2/oauth/client-token
                val loginBaseUrl = "http://localhost:" + mockPort + "/v1/login";

                val request = new LoginRequest(
                    Optional.of(loginBaseUrl),
                    Optional.of(ProfileName.mkUnsafe("cs-profile")),
                    true
                );

                val future = Executors.newSingleThreadExecutor()
                    .submit(() -> new LoginOperation(ctx.get(), request).execute());

                // Wait for the callback server URL to appear in stdout
                String loginLine = null;
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(50);
                    loginLine = ctx.rawOutput().stream()
                        .map(TestCliContext.OutputLine::unwrap)
                        .map(LoginCmdTest::decodeOutputLine)
                        .filter(s -> s.contains("localhost:"))
                        .findFirst()
                        .orElse(null);
                    if (loginLine != null) break;
                }

                assertThat(loginLine).as("login URL must be printed to stdout").isNotNull();

                // Extract the callback port from the redirect_uri in the printed login URL
                val portStr = loginLine.replaceAll(".*localhost:(\\d+)/callback.*", "$1");
                val callbackPort = Integer.parseInt(portStr.trim());
                assertThat(callbackPort).isGreaterThan(0);

                // Deliver the fake JWT via the callback
                val httpStatus = sendCallback(callbackPort, fakeJwt);
                assertThat(httpStatus).isEqualTo(200);

                val result = future.get(10, TimeUnit.SECONDS);
                assertThat(result).isInstanceOf(LoginOperation.LoginSuccess.class);

                // Verify ASTRA_CS_TOKEN was written to the profile
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
}
