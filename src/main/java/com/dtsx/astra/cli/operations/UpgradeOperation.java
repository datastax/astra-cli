package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.core.CliProperties.ExternalSoftware;
import com.dtsx.astra.cli.core.datatypes.Unit;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.utils.HttpUtils;
import com.dtsx.astra.cli.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import static com.dtsx.astra.cli.core.datatypes.Unit.Unit;

@RequiredArgsConstructor
public class UpgradeOperation implements Operation<Unit> {
    private final CliContext ctx;
    private final DownloadsGateway downloadsGateway;
    private final UpgradeRequest request;

    public sealed interface VersionType {}
    public record SpecificVersion(String version) implements VersionType {}
    public record LatestVersion(boolean includePreReleases) implements VersionType {}

    public record UpgradeRequest(
        VersionType versionType,
        BiConsumer<String, String> confirmUpgrade
    ) {}

    @Override
    public Unit execute() {
        val currentExePath = resolveCurrentExePath();

        if (!Files.isWritable(currentExePath)) {
            throw new AstraCliException(ExitCode.UNSUPPORTED_EXECUTION, """
              @|bold,red No write permissions to update the current process @|faint,italic (%s)|@|@
            
              Is the binary managed by a package manager (e.g. nix), or are you not running Astra CLI as a binary?
            """.formatted(currentExePath));
        }

        val version = ctx.log().loading("Resolving the version to download", (_) -> (
            resolveReleaseVersion(request.versionType()).replaceFirst("^[vV]", "").trim()
        ));

        val platform = resolveCurrentPlatform();

        val newExePath = downloadsGateway.downloadAstra(new ExternalSoftware(
            "https://github.com/" + CliProperties.cliGithubRepo() + "/releases/download/v" + version + "/" + CliProperties.cliName() + "-" + platform + (ctx.isWindows() ? ".zip" : ".tar.gz"),
            version
        ));

        if (newExePath.isLeft()) {
            throw new AstraCliException(ExitCode.DOWNLOAD_ISSUE, """
              @|bold,red An error occurred while downloading the new version of Astra CLI (v%s): %s|@
            """.formatted(version, newExePath.getLeft()));
        }

        val backupMvCmd = ctx.isWindows()
            ? "rename %s %s".formatted(newExePath.getRight(), currentExePath)
            : "mv %s %s".formatted(newExePath.getRight(), currentExePath);

        request.confirmUpgrade().accept(version, backupMvCmd);

        return (ctx.isWindows())
            ? upgradeWindowsExe(newExePath.getRight())
            : upgradeUnixBinary(newExePath.getRight());
    }

    @SneakyThrows
    private Unit upgradeWindowsExe(Path exePath) {
        val pid = ProcessHandle.current().pid();

        new ProcessBuilder("cmd.exe", "/c", """
          for /L %%i in (1,1,60) do (tasklist /FI \\"PID eq %d\\" 2>NUL | find /I \\"%d\\" >NUL && (timeout /T 1 /NOBREAK >NUL) || (move /Y \\"%s\\" \\"%s\\" & exit))
        """.formatted(
            pid,
            pid,
            exePath,
            resolveCurrentExePath()
        )).start();

        return Unit;
    }

    @SneakyThrows
    private Unit upgradeUnixBinary(Path exePath) {
        new ProcessBuilder("sh", "-c", """
          mv -f "%s" "%s" && chmod +x "%s"
        """.formatted(
            exePath,
            resolveCurrentExePath(),
            resolveCurrentExePath()
        )).start();

        return Unit;
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

    private String resolveReleaseVersion(VersionType versionType) {
        return switch (versionType) {
            case SpecificVersion(var version) -> (version.trim().equalsIgnoreCase("latest"))
                ? fetchLatestFullRelease()
                : version.trim();

            case LatestVersion(var includePreReleases) -> (includePreReleases)
                ? fetchLatestIncPreRelease()
                : fetchLatestFullRelease();
        };
    }

    private String fetchLatestFullRelease() {
        return ctx.log().loading("Resolving latest full release of @!astra!@", (_) -> {
            val endpoint = "https://api.github.com/repos/" + CliProperties.cliGithubRepo() + "/releases/latest";

            val response = HttpUtils.GET(endpoint, c -> c, r -> r);

            if (response.statusCode() == 404) {
                throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                  @|bold,red Error: Cannot find latest release from @|underline %s|@|@
                """.formatted(endpoint));
            }

            if (response.statusCode() >= 400) {
                throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                  @|bold,red An error occurred while fetching the latest release from %s|@
                
                  Status:
                  @!%d!@
                
                  Body:
                  %s
                """.formatted(endpoint, response.statusCode(), response.body()));
            }

            val json = JsonUtils.readTree(response.body());

            return json.get("tag_name").asText();
        });
    }

    private String fetchLatestIncPreRelease() {
        return ctx.log().loading("Resolving latest release of @!astra!@", (updateMsg) -> {
            var attempt = 1;

            while (true) {
                val endpoint = "https://api.github.com/repos/" + CliProperties.cliGithubRepo() + "/releases?per_page=1";

                val response = HttpUtils.GET(endpoint, c -> c, r -> r);

                if (response.statusCode() >= 400 && response.statusCode() != 404) {
                    throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                      @|bold,red An error occurred while fetching the latest release from %s|@
                    
                      Status:
                      @!%d!@
                    
                      Body:
                      %s
                    """.formatted(endpoint, response.statusCode(), response.body()));
                }

                val json = JsonUtils.readTree(response.body());

                if (response.statusCode() == 404 || json.isEmpty()) {
                    throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                      @|bold,red Error: Cannot find latest release from @|underline %s|@|@
                    """.formatted(endpoint));
                }

                if (json.isArray() && json.get(0).get("draft").asBoolean()) {
                    updateMsg.accept("Resolving latest release of @!astra!@ (attempt %d)".formatted(++attempt));
                    continue;
                }

                return json.get(0).get("tag_name").asText();
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
