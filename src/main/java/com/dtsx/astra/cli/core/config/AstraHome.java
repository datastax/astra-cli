package com.dtsx.astra.cli.core.config;

import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.utils.FileUtils;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;

public class AstraHome {
    public static final Path DIR = resolveDefaultAstraHomeFolder();

    public static class Dirs {
        private static final Path SCB = DIR.resolve("scb");
        private static final Path COMPLETIONS_CACHE =  DIR.resolve("completions-cache");
        private static final Path LOGS = DIR.resolve("logs");
        private static final Path CQLSH = DIR.resolve("cqlsh-astra");

        public static Path useScb() {
            FileUtils.createDirIfNotExists(SCB, "");
            return SCB;
        }

        public static Path useCompletionsCache() {
            FileUtils.createDirIfNotExists(COMPLETIONS_CACHE, "");
            return COMPLETIONS_CACHE;
        }

        public static Path useLogs() {
            FileUtils.createDirIfNotExists(LOGS, "");
            return LOGS;
        }

        public static Path useCqlsh() {
            FileUtils.createDirIfNotExists(CQLSH, "");
            return CQLSH;
        }

        public static Path useDsbulk(String version) {
            val path = DIR.resolve("dsbulk-" + version);
            FileUtils.createDirIfNotExists(path, "");
            return path;
        }

        public static Path usePulsar(String version) {
            val path = DIR.resolve("lunastreaming-shell-" + version);
            FileUtils.createDirIfNotExists(path, "");
            return path;
        }

        public static boolean cqlshExists() {
            return Files.exists(CQLSH);
        }

        public static boolean dsbulkExists(String version) {
            val path = DIR.resolve("dsbulk-" + version);
            return Files.exists(path);
        }

        public static boolean pulsarExists(String version) {
            val path = DIR.resolve("lunastreaming-shell-" + version);
            return Files.exists(path);
        }
    }

    public static boolean exists() {
        return Files.exists(DIR);
    }

    private static Path resolveDefaultAstraHomeFolder() {
        return CliProperties.defaultHomeFolder();
    }
}
