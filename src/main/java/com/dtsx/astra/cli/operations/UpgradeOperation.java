package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.core.CliProperties.ExternalSoftware;
import com.dtsx.astra.cli.core.datatypes.Unit;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.Version;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.gateways.upgrade.UpgradeGateway;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import static com.dtsx.astra.cli.core.datatypes.Unit.INSTANCE;

@RequiredArgsConstructor
public class UpgradeOperation implements Operation<Unit> {
    private final CliContext ctx;
    private final DownloadsGateway downloadsGateway;
    private final UpgradeGateway upgradeGateway;
    private final UpgradeRequest request;

    public sealed interface VersionType {}
    public record SpecificVersion(Version version) implements VersionType {}
    public record LatestVersion(boolean includePreReleases) implements VersionType {}

    public record UpgradeRequest(
        VersionType versionType,
        boolean allowSameVersion,
        BiConsumer<Version, String> confirmUpgrade
    ) {}

    @Override
    public Unit execute() {
        val currentExePath = resolveCurrentExePath();
        val version = resolveReleaseVersion(request.versionType);

        val platform = resolveCurrentPlatform();

        val newExePath = downloadsGateway.downloadAstra(new ExternalSoftware(
            CliProperties.cliGithubRepoUrl() + "/releases/download/v" + version + "/" + CliProperties.cliName() + "-" + platform + (ctx.isWindows() ? ".zip" : ".tar.gz"),
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
            ? upgradeWindowsExe(currentExePath, newExePath.getRight())
            : upgradeUnixBinary(currentExePath, newExePath.getRight());
    }

    @SneakyThrows
    private Unit upgradeWindowsExe(Path currentExePath, Path newExePath) {
        val pid = ProcessHandle.current().pid();

        new ProcessBuilder("cmd.exe", "/c", """
          for /L %%i in (1,1,60) do (tasklist /FI \\"PID eq %d\\" 2>NUL | find /I \\"%d\\" >NUL && (timeout /T 1 /NOBREAK >NUL) || (move /Y \\"%s\\" \\"%s\\" & exit))
        """.formatted(
            pid,
            pid,
            newExePath,
            currentExePath
        )).start();

        return INSTANCE;
    }

    @SneakyThrows
    private Unit upgradeUnixBinary(Path currentExePath, Path newExePath) {
        new ProcessBuilder("sh", "-c", """
          mv -f "%s" "%s" && chmod +x "%s"
        """.formatted(
            newExePath,
            currentExePath,
            currentExePath
        )).start();

        return INSTANCE;
    }

    private Path resolveCurrentExePath() {
        val currentExePath = ProcessHandle.current().info().command().map(ctx::path);

        if (currentExePath.isEmpty()) {
            throw new AstraCliException(ExitCode.UNSUPPORTED_EXECUTION, """
              @|bold,red Cannot resolve current executable path, are you running Astra CLI as a binary?|@
            """);
        }

        if (!Files.isWritable(currentExePath.get())) {
            throw new AstraCliException(ExitCode.UNSUPPORTED_EXECUTION, """
              @|bold,red No write permissions to update the current process @|faint,italic (%s)|@|@

              Is the binary managed by a package manager (e.g. nix), or are you not running Astra CLI as a binary?
            """.formatted(currentExePath));
        }

        return currentExePath.get();
    }

    private Version resolveReleaseVersion(VersionType versionType) {
        return switch (versionType) {
            case SpecificVersion(var version) -> {
                if (version.major() < 1) {
                    throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                      @|bold,red Error: Can not downgrade to a version prior to 1.0.0 (you asked for v%s)|@
                    """.formatted(version));
                }

                if (version.equals(CliProperties.version()) && !request.allowSameVersion()) {
                    throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                      @|bold,red Error: You are already using Astra CLI v%s|@
                    """.formatted(version));
                }

                yield version;
            }

            case LatestVersion(var includePreReleases) -> {
                val latest = upgradeGateway.latestVersion(includePreReleases);

                if (latest.compareTo(CliProperties.version()) < 1) {
                    throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                      @|bold,red Error: No newer version available (latest is v%s, you have v%s)|@
                    """.formatted(latest, CliProperties.version()));
                }

                yield latest;
            }
        };
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
