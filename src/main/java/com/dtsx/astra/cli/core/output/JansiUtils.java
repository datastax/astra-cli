package com.dtsx.astra.cli.core.output;

import org.fusesource.jansi.internal.Kernel32;
import picocli.jansi.graalvm.AnsiConsole;

public class JansiUtils {
    public static AutoCloseable installIfNecessary() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            Kernel32.SetConsoleOutputCP(65001);
        }
        AnsiConsole.systemInstall();
        return AnsiConsole::systemUninstall;
    }
}
