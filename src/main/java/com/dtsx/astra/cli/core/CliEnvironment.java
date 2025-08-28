package com.dtsx.astra.cli.core;

public class CliEnvironment {
    // Not really unsafe; just to discourage use outside of context initialization
    public static boolean unsafeIsWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    // Not really unsafe; just to discourage use outside of context initialization
    public static boolean unsafeIsTty() {
        return System.console() != null && System.console().isTerminal();
    }
}
