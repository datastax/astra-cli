package com.dtsx.astra.cli;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.Properties;

@UtilityClass
public class CLIProperties {
    private static Properties properties;

    @SneakyThrows
    public static String read(String key) {
        if (properties == null) {
            properties = new Properties();
            properties.load(AstraCli.class.getClassLoader().getResourceAsStream("application.properties"));
        }
        return properties.getProperty(key);
    }
}
