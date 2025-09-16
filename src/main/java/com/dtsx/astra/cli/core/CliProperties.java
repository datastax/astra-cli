package com.dtsx.astra.cli.core;

import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import lombok.Cleanup;
import lombok.val;
import org.graalvm.nativeimage.ImageInfo;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.IVersionProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.dtsx.astra.cli.core.CliEnvironment.unsafeIsWindows;

public class CliProperties implements IVersionProvider {
    static {
        for (val file : List.of("static.properties", "dynamic.properties")) {
            try {
                @Cleanup val stream = CliProperties.class.getClassLoader().getResourceAsStream(file);

                if (stream == null) {
                    throw new CongratsYouFoundABugException("Could not find resource '" + file + "'' in classpath.");
                }

                System.getProperties().load(stream);
            } catch (IOException e) {
                throw new CongratsYouFoundABugException("Could not read '" + file + "' - '" + e.getMessage() + "'", e);
            }
        }
        cliName(); // load cli.name into system properties
        defaultRcFile(unsafeIsWindows());
        defaultHomeFolder(unsafeIsWindows());
    }

    public record ExternalSoftware(String url, String version) {}

    public static ExternalSoftware cqlsh() {
        return new ExternalSoftware(prop("cqlsh.url"), prop("cqlsh.version"));
    }

    public static ExternalSoftware dsbulk() {
        return new ExternalSoftware(prop("dsbulk.url"), prop("dsbulk.version"));
    }

    public static ExternalSoftware pulsar() {
        return new ExternalSoftware(prop("pulsar-shell.url"), prop("pulsar-shell.version"));
    }

    public static String version() {
        return prop("cli.version");
    }

    public static String rcFileName() {
        return prop("cli.rc-file-name");
    }

    public static String homeFolderName(boolean useDotPrefix) {
        return ((useDotPrefix) ? "." : "") + prop("cli.home-folder-name");
    }

    private static @Nullable String cachedRcFile = null;

    public static String defaultRcFile(boolean isWindows) {
        if (cachedRcFile != null) {
            return cachedRcFile;
        }

        val customPath = System.getenv(CliProperties.rcEnvVar());

        if (customPath != null) {
            System.setProperty("cli.rc-file-path", customPath);
            cachedRcFile = customPath;
        }
        else if (System.getenv("XDG_CONFIG_HOME") != null) { // TODO - should we do this?
            val path = File.separator + CliProperties.homeFolderName(false) + File.separator + CliProperties.rcFileName();

            System.setProperty("cli.rc-file-path", (isWindows ? "%XDG_CONFIG_HOME%" : "$XDG_CONFIG_HOME") + path);
            cachedRcFile = System.getenv("XDG_CONFIG_HOME") + path;
        }
        else {
            val path = File.separator + CliProperties.rcFileName();

            System.setProperty("cli.rc-file-path", (isWindows ? "%USERPROFILE%" : "~") + path);
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

        if (customPath != null) {
            System.setProperty("cli.home-folder-path", customPath);
            cachedHomeFolder = customPath;
        }
        else if (System.getenv("XDG_DATA_HOME") != null) { // TODO - should we do this?
            val path = File.separator + CliProperties.homeFolderName(false);

            System.setProperty("cli.home-folder-path", (isWindows ? "%XDG_DATA_HOME%" : "$XDG_DATA_HOME") + path);
            cachedHomeFolder = System.getenv("XDG_DATA_HOME") + path;
        }
        else {
            val base = (isWindows)
                ? System.getenv("LOCALAPPDATA")
                : System.getProperty("user.home");

            val path = File.separator + CliProperties.homeFolderName(true);

            System.setProperty("cli.home-folder-path", (isWindows ? "%LOCALAPPDATA%" : "~") + path);
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
        return prop("cli.env-vars.rc-file");
    }

    public static String homeEnvVar() {
        return prop("cli.env-vars.home-folder");
    }

    @Override
    public String[] getVersion() {
        return new String[]{ version() };
    }

    private static String prop(String string) {
        val value = System.getProperty(string);

        if (value == null) {
            throw new CongratsYouFoundABugException("Could not find property '%s' in System properties".formatted(string));
        }

        return value;
    }
}
