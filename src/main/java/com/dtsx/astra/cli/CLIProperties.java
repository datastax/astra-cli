package com.dtsx.astra.cli;

import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import lombok.Cleanup;
import lombok.val;
import picocli.CommandLine.IVersionProvider;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public class CLIProperties implements IVersionProvider {
    private static Properties properties;
    private static String version;

    public static String read(String key) {
        try {
            if (properties == null) {
                properties = new Properties();
                properties.load(AstraCli.class.getClassLoader().getResourceAsStream("application.properties"));
            }
            return properties.getProperty(key);
        } catch (Exception e) {
            throw new CongratsYouFoundABugException("Could not read property '" + key + "' from application.properties - '" + e.getMessage() + "'", e);
        }
    }

    public static String version() {
        try {
            if (version == null) {
                @Cleanup val stream = Objects.requireNonNull(AstraCli.class.getClassLoader().getResourceAsStream("version.txt"));
                version = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
            return version;
        } catch (Exception e) {
            throw new CongratsYouFoundABugException("Could not read version from version.txt - '" + e.getMessage() + "'", e);
        }
    }

    @Override
    public String[] getVersion() {
        return new String[]{ version() };
    }
}
