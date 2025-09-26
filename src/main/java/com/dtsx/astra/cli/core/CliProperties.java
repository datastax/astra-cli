package com.dtsx.astra.cli.core;

import com.dtsx.astra.cli.core.CliEnvironment.OS;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.models.Version;
import lombok.Cleanup;
import lombok.val;
import org.graalvm.nativeimage.ImageInfo;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.IVersionProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.dtsx.astra.cli.core.CliEnvironment.unsafeResolvePlatform;

public class CliProperties implements IVersionProvider {
    static {
        for (val file : List.of("static.properties", "dynamic.properties")) {
            try {
                @Cleanup val stream = CliProperties.class.getClassLoader().getResourceAsStream(file);

                if (stream == null) {
                    throw new CongratsYouFoundABugException("Could not find resource '" + file + "' in classpath.");
                }

                System.getProperties().load(stream);
            } catch (IOException e) {
                throw new CongratsYouFoundABugException("Could not read '" + file + "' - '" + e.getMessage() + "'", e);
            }
        }
        cliName(); // load cli.name into system properties
        defaultRcFile(unsafeResolvePlatform().os() == OS.WINDOWS);
        defaultHomeFolder(unsafeResolvePlatform().os() == OS.WINDOWS);
    }

    public record ExternalSoftware(String url, Version version) {}

    public static ExternalSoftware cqlsh() {
        return new ExternalSoftware(prop("cqlsh.url"), Version.mkUnsafe(prop("cqlsh.version")));
    }

    public static ExternalSoftware dsbulk() {
        return new ExternalSoftware(prop("dsbulk.url"), Version.mkUnsafe(prop("dsbulk.version")));
    }

    public static ExternalSoftware pulsar() {
        return new ExternalSoftware(prop("pulsar-shell.url"), Version.mkUnsafe(prop("pulsar-shell.version")));
    }

    public static Version version() {
        return Version.mkUnsafe(prop("cli.version"));
    }

    public static String rcFileName() {
        return prop("cli.rc-file.name");
    }

    public static String homeFolderName(boolean useDotPrefix) {
        return ((useDotPrefix) ? "." : "") + prop("cli.home-folder.name");
    }

    public static String cliGithubRepoUrl() {
        return prop("cli.github.urls.repo=");
    }

    public static String cliGithubApiReposUrl() {
        return prop("cli.github.urls.api.repos");
    }

    private static @Nullable String cachedRcFile = null;

    public static class FileResolvers {
        public static final String CUSTOM = "custom";
        public static final String XDG = "xdg";
        public static final String HOME = "home";
    }

    public static String defaultRcFile(boolean isWindows) {
        if (cachedRcFile != null) {
            return cachedRcFile;
        }

        val customPath = System.getenv(CliProperties.rcEnvVar());
        val xdgConfigHome = System.getenv("XDG_CONFIG_HOME");

        if (customPath != null && !customPath.isBlank()) {
            System.setProperty("cli.rc-file.path", customPath);
            System.setProperty("cli.rc-file.resolver", FileResolvers.CUSTOM);
            cachedRcFile = customPath;
        }
        else if (xdgConfigHome != null && !xdgConfigHome.isBlank()) { // TODO - should we do this?
            val path = File.separator + CliProperties.homeFolderName(false) + File.separator + CliProperties.rcFileName();

            System.setProperty("cli.rc-file.path", (isWindows ? "%XDG_CONFIG_HOME%" : "$XDG_CONFIG_HOME") + path);
            System.setProperty("cli.rc-file.resolver", FileResolvers.XDG);
            cachedRcFile = xdgConfigHome + path;
        }
        else {
            val path = File.separator + CliProperties.rcFileName();

            System.setProperty("cli.rc-file.path", (isWindows ? "%USERPROFILE%" : "~") + path);
            System.setProperty("cli.rc-file.resolver", FileResolvers.HOME);
            cachedRcFile = System.getProperty("user.home") + path;
        }

        return cachedRcFile;
    }

    private static @Nullable String cachedHomeFolder = null;

    public static String defaultHomeFolder(boolean isWindows) {
        if (cachedHomeFolder != null) {
            return cachedHomeFolder;
        }

        val customPath = System.getenv(CliProperties.homeEnvVar());
        val xdgDataHome = System.getenv("XDG_DATA_HOME");

        if (customPath != null && !customPath.isBlank()) {
            System.setProperty("cli.home-folder.path", customPath);
            System.setProperty("cli.home-folder.resolver", FileResolvers.CUSTOM);
            cachedHomeFolder = customPath;
        }
        else if (xdgDataHome != null && !xdgDataHome.isBlank()) { // TODO - should we do this?
            val path = File.separator + CliProperties.homeFolderName(false);

            System.setProperty("cli.home-folder.path", (isWindows ? "%XDG_DATA_HOME%" : "$XDG_DATA_HOME") + path);
            System.setProperty("cli.home-folder.resolver", FileResolvers.XDG);
            cachedHomeFolder = xdgDataHome + path;
        }
        else {
            val base = (isWindows)
                ? System.getenv("LOCALAPPDATA")
                : System.getProperty("user.home");

            val path = File.separator + CliProperties.homeFolderName(true);

            System.setProperty("cli.home-folder.path", (isWindows ? "%LOCALAPPDATA%" : "~") + path);
            System.setProperty("cli.home-folder.resolver", FileResolvers.HOME);
            cachedHomeFolder = base + path;
        }

        return cachedHomeFolder;
    }

    private static @Nullable String cachedCliName = null;

    public static String cliName() {
        if (cachedCliName != null) {
            return cachedCliName;
        }

        val path = ProcessHandle.current()
            .info()
            .command()
            .map(Path::of);

        cachedCliName = (path.isPresent() && ImageInfo.inImageCode())
            ? path.get().getFileName().toString()
            : "astra";

        System.setProperty("cli.name", cachedCliName);
        return cachedCliName;
    }

    public static String rcEnvVar() {
        return prop("cli.rc-file.env-var");
    }

    public static String homeEnvVar() {
        return prop("cli.home-folder.env-var");
    }

    @Override
    public String[] getVersion() {
        return new String[]{ version().toString() };
    }

    private static String prop(String string) {
        val value = System.getProperty(string);

        if (value == null) {
            throw new CongratsYouFoundABugException("Could not find property '%s' in System properties".formatted(string));
        }

        return value;
    }
}
