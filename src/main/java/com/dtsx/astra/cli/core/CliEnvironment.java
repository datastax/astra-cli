package com.dtsx.astra.cli.core;

import lombok.val;

public class CliEnvironment {
    public enum OS { WINDOWS, LINUX, MAC, OTHER }
    public enum Arch { X86_64, ARM, OTHER }

    public record Platform(OS os, Arch arch) {}

    public static Platform unsafeResolvePlatform() {
        val osName = System.getProperty("os.name").toLowerCase();

        val os =
            osName.contains("win")
                ? OS.WINDOWS
            : osName.contains("nux")
                ? OS.LINUX :
            osName.contains("mac")
                ? OS.MAC
                : OS.OTHER;

        val archName = System.getProperty("os.arch").toLowerCase();

        val arch =
            archName.contains("amd64") || archName.contains("x86_64")
                ? Arch.X86_64
            : archName.contains("aarch64") || archName.contains("arm64")
                ? Arch.ARM
                : Arch.OTHER;

        return new Platform(os, arch);
    }

    // Not really unsafe; just to discourage use outside of context initialization
    public static boolean unsafeIsTty() {
        return System.console() != null && System.console().isTerminal();
    }
}
