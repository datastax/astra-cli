package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.core.CliProperties.ExternalSoftware;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.utils.JsonUtils;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class UpgradeOperation implements Operation<Void> {
    private final CliContext ctx;
    private final DownloadsGateway downloadsGateway;
    private final UpgradeRequest request;

    public record UpgradeRequest(
        Optional<String> version,
        BiConsumer<String, String> confirmUpgrade
    ) {}

    @Override
    public Void execute() {
        val currentExePath = resolveCurrentExePath();

        if (!Files.isWritable(currentExePath)) {
            throw new AstraCliException(ExitCode.UNSUPPORTED_EXECUTION, """
              @|bold,red No write permissions to update the astra binary @faint,italic (@|underline %s|@)|@
            """.formatted(currentExePath));
        }

        val version = ctx.log().loading("Resolving the version to download", (_) -> (
            "v" + resolveReleaseVersion(request.version()).replaceFirst("^[vV]", "")
        ));

        val platform = resolveCurrentPlatform();

        val newExePath = ctx.log().loading("Downloading astra-cli " + version, (_) -> (
            downloadsGateway.downloadAstra(new ExternalSoftware(
                "api.github.com/repos/" + CliProperties.cliGithubRepo() + "/releases/download/v" + version + "/" + CliProperties.cliName() + "-" + platform + (ctx.isWindows() ? ".zip" : ".tar.gz"),
                version
            ))
        ));

        if (newExePath.isLeft()) {
            throw new AstraCliException(ExitCode.DOWNLOAD_ISSUE, """
              @|bold,red An error occurred while downloading the new version of Astra CLI (%s): %s|@
            """.formatted(version, newExePath));
        }

        val backupMvCmd = ctx.isWindows()
            ? "rename %s %s.bak".formatted(currentExePath, newExePath)
            : "mv %s %s.bak".formatted(currentExePath, newExePath);

        request.confirmUpgrade().accept(version, backupMvCmd);

        return (ctx.isWindows())
            ? upgradeWindowsExe(newExePath.getRight())
            : upgradeUnixBinary(newExePath.getRight());
    }

    @SneakyThrows
    private Void upgradeWindowsExe(Path exePath) {
        val pid = ProcessHandle.current().pid();

        new ProcessBuilder("cmd.exe", "/c", """
          for /L %%i in (1,1,60) do (tasklist /FI \\"PID eq %d\\" 2>NUL | find /I \\"%d\\" >NUL && (timeout /T 1 /NOBREAK >NUL) || (move /Y \\"%s\\" \\"%s\\" & exit))
        """.formatted(
            pid,
            pid,
            exePath,
            resolveCurrentExePath()
        )).start();

        return null;
    }

    @SneakyThrows
    private Void upgradeUnixBinary(Path exePath) {
        new ProcessBuilder("sh", "-c", """
          mv -f "%s" "%s" && chmod +x "%s"
        """.formatted(
            exePath,
            resolveCurrentExePath(),
            resolveCurrentExePath()
        )).start();

        return null;
    }

    private Path resolveCurrentExePath() {
        val currentExePath = ProcessHandle.current().info().command().map(ctx::path);

        if (currentExePath.isEmpty()) {
            throw new AstraCliException(ExitCode.UNSUPPORTED_EXECUTION, """
              @|bold,red Cannot resolve current executable path, are you running Astra CLI as a binary?|@
            """);
        }

        return currentExePath.get();
    }

    private String resolveReleaseVersion(Optional<String> requestedVersion) {
        return requestedVersion.filter(v -> v.trim().equalsIgnoreCase("latest")).orElseGet(() -> {
            try {
                val endpoint = "api.github.com/repos/" + CliProperties.cliGithubRepo() + "/releases/latest";

                @Cleanup val client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();

                val request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/vnd.github+json")
                    .GET()
                    .build();

                val response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 404) {
                    throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, "Cannot find latest release from " + endpoint);
                }

                if (response.statusCode() != 200) {
                    throw new AstraCliException(ExitCode.UNCAUGHT, "Error when fetching latest version from " + endpoint + ", status=" + response.statusCode() + ", body=" + response.body());
                }

                val json = JsonUtils.objectMapper().readTree(response.body());

                return json.get("tag_name").asText();
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
    }

    private String resolveCurrentPlatform() {
        val platform = ctx.platform();

        val osStr = switch (platform.os()) {
            case LINUX -> "linux";
            case MAC -> "macos";
            case WINDOWS -> "windows";
            default -> throw new AstraCliException(ExitCode.PLATFORM_ISSUE, "Unknown OS: " + platform.os());
        };

        val archStr = switch (platform.arch()) {
            case X86_64 -> "x86_64";
            case ARM -> "arm64";
            default -> throw new AstraCliException(ExitCode.PLATFORM_ISSUE, "Unknown Architecture: " + platform.arch());
        };

        return osStr + "-" + archStr;
    }
}
