package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Unit;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.Version;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.properties.CliProperties.AstraJar;
import com.dtsx.astra.cli.core.properties.CliProperties.ExternalSoftware;
import com.dtsx.astra.cli.core.properties.CliProperties.SupportedPackageManager;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.gateways.upgrade.UpgradeGateway;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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
        boolean force,
        List<String> originalArgs,
        BiConsumer<Version, String> confirmUpgrade
    ) {}

    @Override
    public Unit execute() {
        val currentExePath = resolveCurrentExePath();
        val version = resolveReleaseVersion(request.versionType);

        val platform = resolveCurrentPlatform();

        val newExePath = downloadsGateway.downloadAstra(new ExternalSoftware(
            ctx.properties().cliGithubRepoUrl() + "/releases/download/v" + version + "/" + ctx.properties().cliName() + "-" + platform + (ctx.isWindows() ? ".zip" : ".tar.gz"),
            version.toString()
        ));

        if (newExePath.isLeft()) {
            throw new AstraCliException(ExitCode.DOWNLOAD_ISSUE, """
              @|bold,red An error occurred while downloading the new version of Astra CLI (v%s): %s|@
            """.formatted(version, newExePath.getLeft()));
        }

        val backupMvCmd = ctx.isWindows()
            ? "move /Y \"%s\" \"%s\"".formatted(newExePath.getRight(), currentExePath)
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
          for /L %%i in (1,1,60) do (tasklist /FI \\"PID eq %d\\" /NH 2>NUL | find \\"%d\\" >NUL && (timeout /T 1 /NOBREAK >NUL) || (move /Y \\"%s\\" \\"%s\\" && exit || exit /B 1))
        """.formatted(
            pid,
            pid,
            newExePath,
            currentExePath
        )).start();

        return Unit.INSTANCE;
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

        return Unit.INSTANCE;
    }

    private Path resolveCurrentExePath() {
        val binaryPath = ctx.properties().cliPath(ctx);

        if (binaryPath instanceof AstraJar) {
            throw new AstraCliException(ExitCode.UNSUPPORTED_EXECUTION, """
              @|bold,red Error: Cannot run this command when Astra CLI is not being run as a binary|@
            """);
        }

        val hints = new ArrayList<Hint>() {{
            add(new Hint("(Not recommended) Attempt an update anyways", request.originalArgs(), "--force-unsafe"));
        }};

        ctx.properties().owningPackageManager().ifPresent((pm) -> {
            if (request.force) {
                ctx.log().warn("Forcing self-upgrade even though ${cli.name} is managed by package manager (%s)".formatted(pm.displayName()));
                return;
            }

            if (pm == SupportedPackageManager.BREW) {
                hints.addFirst(new Hint("Use Homebrew to update Astra CLI", "brew upgrade ${cli.name}"));
            }

            throw new AstraCliException(ExitCode.UNSUPPORTED_EXECUTION, """
              @|bold,red Error: Cannot self-update when Astra CLI is managed by a package manager (%s)|@

              Please use the package manager to update Astra CLI.
            """.formatted(pm.displayName()), hints);
        });

        if (!Files.isWritable(binaryPath.unwrap())) {
            throw new AstraCliException(ExitCode.UNSUPPORTED_EXECUTION, """
              @|bold,red Error: No write permissions to update the current process @|faint,italic (%s)|@|@

              Is the binary managed by a package manager (e.g. nix), or are you not running Astra CLI as a binary?
            """.formatted(binaryPath), hints);
        }

        return binaryPath.unwrap();
    }

    private Version resolveReleaseVersion(VersionType versionType) {
        return switch (versionType) {
            case SpecificVersion(var version) -> {
                if (version.major() < 1) {
                    throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                      @|bold,red Error: Can not downgrade to a version prior to 1.0.0 (you asked for v%s)|@
                    """.formatted(version));
                }

                if (version.equals(ctx.properties().version()) && !request.allowSameVersion) {
                    throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                      @|bold,red Error: You are already using Astra CLI v%s|@
                    """.formatted(version));
                }

                yield version;
            }

            case LatestVersion(var includePreReleases) -> {
                val latest = upgradeGateway.latestVersion(includePreReleases);

                if (latest.equals(ctx.properties().version()) && !request.allowSameVersion) {
                    throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                      @|bold,red Error: You are already using the latest Astra CLI version (v%s)|@
                    """.formatted(latest));
                }

                if (latest.compareTo(ctx.properties().version()) < 0) {
                    throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                      @|bold,red Error: No newer version available (latest is v%s, you have v%s)|@
                    """.formatted(latest, ctx.properties().version()));
                }

                yield latest;
            }
        };
    }

    private String resolveCurrentPlatform() {
        val platform = ctx.env().platform();

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
