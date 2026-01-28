package com.dtsx.astra.cli.core.properties;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.models.Version;
import lombok.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface CliProperties {
    class ConstEnvVars {
        public static final String IGNORE_MULTIPLE_PATHS = "ASTRA_IGNORE_MULTIPLE_PATHS";
        public static final String IGNORE_BETA_WARNINGS = "ASTRA_IGNORE_BETA_WARNINGS";
        public static final String NO_UPDATE_NOTIFIER = "ASTRA_NO_UPDATE_NOTIFIER";
        public static final String COMPLETIONS_SETUP = "ASTRA_COMPLETIONS_SETUP";
        public static final String DEFAULT_ARGS = "ASTRA_DEFAULT_ARGS";
        public static final String PROFILE = "ASTRA_PROFILE";
    }

    record ExternalSoftware(
        String url,
        String version // can't use Version here unfortunately b/c pulsar-shell doesn't use semver ._.
    ) {}

    enum PathLocationResolver { CUSTOM, XDG, HOME }

    @RequiredArgsConstructor
    class PathLocation {
        private final String path;
        private final PathLocationResolver resolver;

        public static PathLocation from(String path, PathLocationResolver resolver) {
            return new PathLocation(path, resolver);
        }

        public Path path(CliContext ctx) {
            return ctx.absPath(this.path);
        }

        public PathLocationResolver resolver() {
            return resolver;
        }
    }

    @Value
    class PathLocations {
        NEList<PathLocation> all;

        public Path preferred(CliContext ctx) {
            return all.getLast().path(ctx);
        }
    }

    @Getter
        @RequiredArgsConstructor
    enum SupportedPackageManager {
        BREW("Homebrew"),
        NIX("Nix");
        private final String displayName;
    }

    sealed interface PathToAstra { Path unwrap(); }
    record AstraBinary(Path unwrap) implements PathToAstra {}
    record AstraJar(Path unwrap) implements PathToAstra {}

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

    String useProfile();

    boolean disableBetaWarnings();

    boolean noUpgradeNotifications();

    PathToAstra cliPath(CliContext ctx);

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
        val existingPaths = new HashMap<Path, Set<PathLocationResolver>>();

        for (val loc : locations) {
            val path = loc.path(ctx);

            if (Files.exists(path)) {
                existingPaths.computeIfAbsent(path, _ -> new HashSet<>()).add(loc.resolver());
            }
        }

        if (existingPaths.size() > 1) {
            ctx.log().warn("Multiple @!" + thing + "!@ were detected at the following locations:");

            for (val e : existingPaths.entrySet()) {
                val path = e.getKey();
                val resolvers = e.getValue();

                val label = resolvers.stream()
                    .map((r) -> switch (r) {
                        case CUSTOM -> "custom path set via environment variable";
                        case XDG -> "using the xdg specification";
                        case HOME -> "default location";
                    })
                    .collect(Collectors.joining(" and "));

                ctx.log().warn(" - " + path + " @|faint (" + label + ")|@");
            }

            ctx.log().warn();
            return true;
        }

        return false;
    }
}
