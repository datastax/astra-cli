package com.dtsx.astra.cli.core;

public class CliEnvironment {
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static boolean isTty() {
        return System.console() != null;
    }
}
