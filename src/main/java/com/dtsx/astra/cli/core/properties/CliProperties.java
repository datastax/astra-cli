package com.dtsx.astra.cli.core.properties;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.models.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public interface CliProperties {
    class ConstEnvVars {
        public static final String IGNORE_MULTIPLE_PATHS = "ASTRA_IGNORE_MULTIPLE_PATHS";
        public static final String IGNORE_BETA_WARNINGS = "ASTRA_IGNORE_BETA_WARNINGS";
        public static final String NO_UPDATE_NOTIFIER = "ASTRA_NO_UPDATE_NOTIFIER";
        public static final String COMPLETIONS_SETUP = "ASTRA_COMPLETIONS_SETUP";
    }

    record ExternalSoftware(
        String url,
        String version // can't use Version here unfortunately b/c pulsar-shell doesn't use semver ._.
    ) {}

    enum PathLocationResolver { CUSTOM, XDG, HOME }

    record PathLocation(
        String path,
        PathLocationResolver resolver
    ) {}

    record PathLocations(
        String preferred,
        NEList<PathLocation> all
    ) {}

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    enum SupportedPackageManager {
        BREW("Homebrew"),
        NIX("Nix");
        private final String displayName;
    }

    ExternalSoftware cqlsh();

    ExternalSoftware dsbulk();

    ExternalSoftware pulsar();

    Version version();

    String rcFileName();

    String homeFolderName(boolean useDotPrefix);

    String cliGithubRepoUrl();

    String cliGithubApiReposUrl();

    PathLocations rcFileLocations(boolean isWindows);

    PathLocations homeFolderLocations(boolean isWindows);

    String cliName();

    String rcEnvVar();

    String homeEnvVar();

    boolean disableBetaWarnings();

    boolean noUpgradeNotifications();

    Optional<Path> binaryPath();

    Optional<SupportedPackageManager> owningPackageManager();

    default void detectDuplicateFileLocations(CliContext ctx) {
        val ignoreMultiplePaths = Optional.ofNullable(System.getenv(ConstEnvVars.IGNORE_MULTIPLE_PATHS)).orElse("false");

        if (!ignoreMultiplePaths.equalsIgnoreCase("false")) {
            return;
        }

        val rcFileLocations = ctx.properties().rcFileLocations(ctx.isWindows()).all();
        val multipleRcFiles = rcFileLocations.size() > 1 && testAndWarnIfMultiplePathsExist(ctx, rcFileLocations, ".astrarc files");

        val homeFolderLocations = ctx.properties().homeFolderLocations(ctx.isWindows()).all();
        val multipleHomeFolders = homeFolderLocations.size() > 1 && testAndWarnIfMultiplePathsExist(ctx, homeFolderLocations, "astra home folders");

        if (multipleRcFiles || multipleHomeFolders) {
            ctx.log().warn("Please either:");
            ctx.log().warn(" - remove or migrate the lower priority files/folders (custom > xdg > default/home)");
            ctx.log().warn(" - set @'!ASTRA_IGNORE_MULTIPLE_PATHS=true!@ or use the @'!--ignore-multiple-paths!@ flag with @'!${cli.name} shellenv!@ to suppress this warning");
        }
    }

    private boolean testAndWarnIfMultiplePathsExist(CliContext ctx, NEList<PathLocation> locations, String thing) {
        val paths = locations.stream().map((loc) -> Pair.of(ctx.path(loc.path()), loc.resolver())).toList();

        val existingPaths = paths.stream().filter((p) -> Files.exists(p.getLeft())).toList();

        if (existingPaths.size() > 1) {
            ctx.log().warn("Multiple @!" + thing + "!@ were detected at the following locations:");

            for (val p : existingPaths) {
                val label = switch (p.getRight()) {
                    case CUSTOM -> "custom path set via environment variable";
                    case XDG -> "using the xdg specification";
                    case HOME -> "default location";
                };

                ctx.log().warn(" - " + p.getLeft() + " @|faint (" + label + ")|@");
                ctx.log().warn();
            }

            return true;
        }

        return false;
    }
}
