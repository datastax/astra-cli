package com.dtsx.astra.cli.core;

import org.jetbrains.annotations.VisibleForTesting;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class CliEnvironment {
    private static FileSystem fs = FileSystems.getDefault();

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static boolean isTty() {
        return System.console() != null && System.console().isTerminal();
    }

    public static FileSystem fs() {
        return fs;
    }

    public static Path path(String first, String... more) {
        return fs.getPath(first, more);
    }

    @VisibleForTesting
    public static void setFileSystem(FileSystem fileSystem) {
        System.out.println("Setting test file system: " + fileSystem);
        fs = fileSystem;
    }
}
