package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.operations.LoginOperation.LoginResult;
import com.dtsx.astra.cli.utils.ShellUtils;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.sun.net.httpserver.HttpServer;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class LoginOperation implements Operation<LoginResult> {
    private static final int TIMEOUT_SECONDS = 120;
    private static final String CALLBACK_PATH = "/callback";

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
          <p>No token was received. Please try again.</p>
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

    @Override
    public LoginResult execute() {
        int port;
        try {
            port = findFreePort();
        } catch (IOException e) {
            return new LoginError("Could not find a free local port: " + e.getMessage());
        }

        val tokenHolder = new AtomicReference<String>();
        val latch = new CountDownLatch(1);

        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        } catch (IOException e) {
            return new LoginError("Could not start local callback server: " + e.getMessage());
        }

        server.createContext(CALLBACK_PATH, exchange -> {
            val query = Optional.ofNullable(exchange.getRequestURI().getQuery()).orElse("");
            val params = parseQueryParams(query);
            val token = params.get("token");

            val html = (token != null && !token.isBlank()) ? SUCCESS_HTML : ERROR_HTML;
            val bytes = html.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (val os = exchange.getResponseBody()) {
                os.write(bytes);
            }

            if (token != null && !token.isBlank()) {
                tokenHolder.set(token);
            }
            latch.countDown();
        });

        server.setExecutor(Executors.newSingleThreadExecutor(r -> {
            val t = new Thread(r, "astra-cli-login-callback");
            t.setDaemon(true);
            return t;
        }));
        server.start();

        try {
            val baseUrl = request.loginBaseUrl()
                .or(() -> Optional.ofNullable(System.getenv("ASTRA_LOGIN_URL")))
                .orElse("https://astra.datastax.com/v1/login");
            val redirectUri = "http://localhost:" + port + CALLBACK_PATH;
            val loginUrl = baseUrl + "?redirect_uri="
                + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&cli=true";

            ctx.console().println("Opening browser to complete login...");
            ctx.console().println("If a browser window does not open, visit:");
            ctx.console().println("  " + loginUrl);
            ctx.console().println();

            ShellUtils.openURL(loginUrl);

            val received = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!received) {
                return new LoginCancelled();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new LoginCancelled();
        } finally {
            server.stop(0);
        }

        val rawToken = tokenHolder.get();
        if (rawToken == null || rawToken.isBlank()) {
            return new LoginError("No token was received in the callback.");
        }

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

        return new LoginSuccess(profileName, request.setAsDefault());
    }

    private static int findFreePort() throws IOException {
        try (val socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

    private static java.util.Map<String, String> parseQueryParams(String query) {
        if (query == null || query.isBlank()) {
            return java.util.Map.of();
        }
        return Arrays.stream(query.split("&"))
            .map(pair -> pair.split("=", 2))
            .filter(parts -> parts.length == 2)
            .collect(Collectors.toMap(
                parts -> URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                parts -> URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
            ));
    }
}
