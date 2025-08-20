package com.dtsx.astra.cli.core.output;

import picocli.jansi.graalvm.AnsiConsole;

public class JansiUtils {
    public static AutoCloseable installIfNecessary() {
        if (System.getenv("WT_SESSION") == null) {
            AnsiConsole.systemInstall();
            return AnsiConsole::systemUninstall;
        }
        return () -> {};
    }
}
