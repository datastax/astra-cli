package com.dtsx.astra.cli;

import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import lombok.Cleanup;
import lombok.val;
import picocli.CommandLine.IVersionProvider;

import java.util.List;

public class CLIProperties implements IVersionProvider {
    static {
        for (val file : List.of("static.properties", "dynamic.properties")) {
            try {
                @Cleanup val stream = CLIProperties.class.getClassLoader().getResourceAsStream(file);

                if (stream == null) {
                    throw new CongratsYouFoundABugException("Could not find resource '" + file + "'' in classpath.");
                }

                System.getProperties().load(stream);
            } catch (Exception e) {
                throw new CongratsYouFoundABugException("Could not read '" + file + "' - '" + e.getMessage() + "'", e);
            }
        }
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
