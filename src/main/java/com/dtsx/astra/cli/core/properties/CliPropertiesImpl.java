package com.dtsx.astra.cli.core.properties;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.models.Version;
import com.dtsx.astra.cli.core.properties.CliEnvironment.OS;
import com.dtsx.astra.cli.utils.FileUtils;
import lombok.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CliPropertiesImpl implements CliProperties {
    private static boolean loaded = false;

    public static CliProperties mkAndLoadSysProps(CliEnvironment env) {
        return mkAndLoadSysProps(env, p -> p);
    }

    public static CliProperties mkAndLoadSysProps(CliEnvironment env, Function<CliProperties, CliProperties> wrapper) {
        val props = wrapper.apply(new CliPropertiesImpl());

        if (loaded) {
            return props;
        }

        for (val file : List.of("static.properties", "dynamic.properties")) {
            try {
                @Cleanup val stream = CliPropertiesImpl.class.getClassLoader().getResourceAsStream(file);

                if (stream == null) {
                    throw new CongratsYouFoundABugException("Could not find resource '" + file + "' in classpath.");
                }

                System.getProperties().load(stream);
            } catch (IOException e) {
                throw new CongratsYouFoundABugException("Could not read '" + file + "' - '" + e.getMessage() + "'", e);
            }
        }

        props.cliName();
        props.binaryPath();
        props.rcFileLocations(env.platform().os() == OS.WINDOWS);
        props.homeFolderLocations(env.platform().os() == OS.WINDOWS);

        loaded = true;
        return props;
    }

    @Override
    public ExternalSoftware cqlsh() {
        return new ExternalSoftware(requireProperty("cqlsh.url"), requireProperty("cqlsh.version"));
    }

    @Override
    public ExternalSoftware dsbulk() {
        return new ExternalSoftware(requireProperty("dsbulk.url"), requireProperty("dsbulk.version"));
    }

    @Override
    public ExternalSoftware pulsar() {
        return new ExternalSoftware(requireProperty("pulsar-shell.url"), requireProperty("pulsar-shell.version"));
    }

    @Override
    public Version version() {
        return Version.mkUnsafe(requireProperty("cli.version"));
    }

    @Override
    public String rcFileName() {
        return requireProperty("cli.rc-file.name");
    }

    @Override
    public String homeFolderName(boolean useDotPrefix) {
        return ((useDotPrefix) ? "." : "") + requireProperty("cli.home-folder.name");
    }

    @Override
    public String cliGithubRepoUrl() {
        return requireProperty("cli.github.urls.repo");
    }

    @Override
    public String cliGithubApiReposUrl() {
        return requireProperty("cli.github.urls.api.repos");
    }

    @Override
    public PathLocations rcFileLocations(boolean isWindows) {
        val locations = new ArrayList<PathLocation>();

        val customPath = System.getenv(rcEnvVar());
        val xdgConfigHome = System.getenv("XDG_CONFIG_HOME");

        {
            val path = File.separator + rcFileName();

            System.setProperty("cli.rc-file.path", (isWindows ? "%USERPROFILE%" : "~") + path);
            System.setProperty("cli.rc-file.resolver", PathLocationResolver.HOME.name());

            locations.add(
                new PathLocation(System.getProperty("user.home") + path, PathLocationResolver.HOME)
            );
        }

        if (xdgConfigHome != null && !xdgConfigHome.isBlank()) {
            val path = File.separator + homeFolderName(false) + File.separator + rcFileName();

            System.setProperty("cli.rc-file.path", (isWindows ? "%XDG_CONFIG_HOME%" : "$XDG_CONFIG_HOME") + path);
            System.setProperty("cli.rc-file.resolver", PathLocationResolver.XDG.name());

            locations.add(
                new PathLocation(xdgConfigHome + path, PathLocationResolver.XDG)
            );
        }

        if (customPath != null && !customPath.isBlank()) {
            System.setProperty("cli.rc-file.path", customPath);
            System.setProperty("cli.rc-file.resolver", PathLocationResolver.CUSTOM.name());

            locations.add(
                new PathLocation(customPath, PathLocationResolver.CUSTOM)
            );
        }

        return new PathLocations(locations.getLast().path(), NEList.parse(locations).orElseThrow(() ->
            new CongratsYouFoundABugException("No .astrarc file locations could be determined")
        ));
    }

    @Override
    public PathLocations homeFolderLocations(boolean isWindows) {
        val locations = new ArrayList<PathLocation>();

        val customPath = System.getenv(homeEnvVar());
        val xdgDataHome = System.getenv("XDG_DATA_HOME");

        {
            val base = (isWindows)
                ? System.getenv("LOCALAPPDATA")
                : System.getProperty("user.home");

            val path = File.separator + homeFolderName(true);

            System.setProperty("cli.home-folder.path", (isWindows ? "%LOCALAPPDATA%" : "~") + path);
            System.setProperty("cli.home-folder.resolver", PathLocationResolver.HOME.name());

            locations.add(
                new PathLocation(base + path, PathLocationResolver.HOME)
            );
        }

        if (xdgDataHome != null && !xdgDataHome.isBlank()) {
            val path = File.separator + homeFolderName(false);

            System.setProperty("cli.home-folder.path", (isWindows ? "%XDG_DATA_HOME%" : "$XDG_DATA_HOME") + path);
            System.setProperty("cli.home-folder.resolver", PathLocationResolver.XDG.name());

            locations.add(
                new PathLocation(xdgDataHome + path, PathLocationResolver.XDG)
            );
        }

        if (customPath != null && !customPath.isBlank()) {
            System.setProperty("cli.home-folder.path", customPath);
            System.setProperty("cli.home-folder.resolver", PathLocationResolver.CUSTOM.name());

            locations.add(
                new PathLocation(customPath, PathLocationResolver.CUSTOM)
            );
        }

        return new PathLocations(locations.getLast().path(), NEList.parse(locations).orElseThrow(() ->
            new CongratsYouFoundABugException("No astra home folder locations could be determined")
        ));
    }

    @Override
    public String cliName() {
        binaryPath().ifPresent((path) -> {
            System.setProperty("cli.name", path.getFileName().toString());
        });

        return System.getProperty("cli.name", "astra");
    }

    @Override
    public String rcEnvVar() {
        return requireProperty("cli.rc-file.env-var");
    }

    @Override
    public String homeEnvVar() {
        return requireProperty("cli.home-folder.env-var");
    }

    @Override
    public boolean disableBetaWarnings() {
        return System.getenv(ConstEnvVars.IGNORE_BETA_WARNINGS) != null;
    }

    @Override
    public boolean noUpgradeNotifications() {
        // latter is for compatibility w/ https://github.com/sindresorhus/update-notifier?tab=readme-ov-file#user-settings
        return System.getenv(ConstEnvVars.NO_UPDATE_NOTIFIER) != null || System.getenv("NO_UPDATE_NOTIFIER") != null;
    }

    @Override
    public Optional<Path> binaryPath() {
        val path = FileUtils.getCurrentBinaryPath();
        path.ifPresent(value -> System.setProperty("cli.path", value.toString()));
        return path;
    }

    @Override
    public Optional<SupportedPackageManager> owningPackageManager() {
        return binaryPath().flatMap((binaryPath) -> {
            if (binaryPath.startsWith("/nix/store/")) {
                return Optional.of(SupportedPackageManager.NIX);
            }
            if (binaryPath.startsWith("/usr/local/") || binaryPath.startsWith("/opt/homebrew/")) {
                return Optional.of(SupportedPackageManager.BREW);
            }
            return Optional.empty();
        });
    }

    protected final String requireProperty(String string) {
        val value = System.getProperty(string);

        if (value == null) {
            throw new CongratsYouFoundABugException("Could not find property '%s' in system properties".formatted(string));
        }

        return value;
    }
}
