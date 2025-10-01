package com.dtsx.astra.cli.core.properties;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.models.Version;
import com.dtsx.astra.cli.core.properties.CliEnvironment.OS;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.graalvm.nativeimage.ImageInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CliPropertiesImpl implements CliProperties {
    public static CliProperties mkAndLoadSysProps(CliEnvironment env) {
        return mkAndLoadSysProps(env, p -> p);
    }

    public static CliProperties mkAndLoadSysProps(CliEnvironment env, Function<CliProperties, CliProperties> wrapper) {
        val props = wrapper.apply(new CliPropertiesImpl());

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
        props.rcFileLocations(env.platform().os() == OS.WINDOWS);
        props.homeFolderLocations(env.platform().os() == OS.WINDOWS);

        return props;
    }

    @Override
    public ExternalSoftware cqlsh() {
        return new ExternalSoftware(prop("cqlsh.url"), Version.mkUnsafe(prop("cqlsh.version")));
    }

    @Override
    public ExternalSoftware dsbulk() {
        return new ExternalSoftware(prop("dsbulk.url"), Version.mkUnsafe(prop("dsbulk.version")));
    }

    @Override
    public ExternalSoftware pulsar() {
        return new ExternalSoftware(prop("pulsar-shell.url"), Version.mkUnsafe(prop("pulsar-shell.version")));
    }

    @Override
    public Version version() {
        return Version.mkUnsafe(prop("cli.version"));
    }

    @Override
    public String rcFileName() {
        return prop("cli.rc-file.name");
    }

    @Override
    public String homeFolderName(boolean useDotPrefix) {
        return ((useDotPrefix) ? "." : "") + prop("cli.home-folder.name");
    }

    @Override
    public String cliGithubRepoUrl() {
        return prop("cli.github.urls.repo");
    }

    @Override
    public String cliGithubApiReposUrl() {
        return prop("cli.github.urls.api.repos");
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
            new CongratsYouFoundABugException("@|bold,red Error: No .astrarc file locations could be determined|@")
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
            new CongratsYouFoundABugException("@|bold,red Error: No astra home folder locations could be determined|@")
        ));
    }

    @Override
    public String cliName() {
        val path = ProcessHandle.current()
            .info()
            .command()
            .map(Path::of);

        val cliName = (path.isPresent() && ImageInfo.inImageCode())
            ? path.get().getFileName().toString()
            : "astra";

        System.setProperty("cli.name", cliName);
        return cliName;
    }

    @Override
    public String rcEnvVar() {
        return prop("cli.rc-file.env-var");
    }

    @Override
    public String homeEnvVar() {
        return prop("cli.home-folder.env-var");
    }

    protected final String prop(String string) {
        val value = System.getProperty(string);

        if (value == null) {
            throw new CongratsYouFoundABugException("Could not find property '%s' in system properties".formatted(string));
        }

        return value;
    }
}
