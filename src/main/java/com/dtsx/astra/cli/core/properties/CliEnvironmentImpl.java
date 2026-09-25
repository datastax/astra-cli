package com.dtsx.astra.cli.core.properties;

import lombok.val;

public class CliEnvironmentImpl implements CliEnvironment {
    @Override
    public Platform platform() {
        val osName = System.getProperty("os.name").toLowerCase();

        val os =
            (osName.contains("win"))
                ? OS.WINDOWS :
            (osName.contains("nux"))
                ? OS.LINUX :
            (osName.contains("mac"))
                ? OS.MAC
                : OS.OTHER;

        val archName = System.getProperty("os.arch").toLowerCase();

        val arch =
            (archName.contains("amd64") || archName.contains("x86_64"))
                ? Arch.X86_64 :
            (archName.contains("aarch64") || archName.contains("arm64"))
                ? Arch.ARM
                : Arch.OTHER;

        return new Platform(os, arch);
    }

    @Override
    public boolean isTty() {
        var console = System.console();
        if (console == null) return false;
        try {
            // Console.isTerminal() is Java 22+; fall back gracefully on Java 21
            var m = console.getClass().getMethod("isTerminal");
            return (boolean) m.invoke(console);
        } catch (ReflectiveOperationException ignored) {
            return true; // pre-22: if console != null, assume TTY
        }
    }
}
