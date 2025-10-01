package com.dtsx.astra.cli.core.properties;

public interface CliEnvironment {
    enum OS { WINDOWS, LINUX, MAC, OTHER }
    enum Arch { X86_64, ARM, OTHER }

    record Platform(OS os, Arch arch) {}

    Platform platform();
    boolean isTty() ;
}
