package com.dtsx.astra.cli.core.config;

import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class AstraHome {
    private boolean isWindows;
    
    public final Path DIR = resolveDefaultAstraHomeFolder();

    public final Dirs Dirs = new Dirs();

    public class Dirs {
        private final Path SCB = DIR.resolve("scb");
        private final Path COMPLETIONS_CACHE =  DIR.resolve("completions-cache");
        private final Path LOGS = DIR.resolve("logs");
        private final Path CQLSH = DIR.resolve("cqlsh-astra");

        public Path useScb() {
            FileUtils.createDirIfNotExists(SCB, "");
            return SCB;
        }

        public Path useCompletionsCache() {
            FileUtils.createDirIfNotExists(COMPLETIONS_CACHE, "");
            return COMPLETIONS_CACHE;
        }

        public Path useLogs() {
            FileUtils.createDirIfNotExists(LOGS, "");
            return LOGS;
        }

        public Path useCqlsh() {
            FileUtils.createDirIfNotExists(CQLSH, "");
            return CQLSH;
        }

        public Path useDsbulk(String version) {
            val path = DIR.resolve("dsbulk-" + version);
            FileUtils.createDirIfNotExists(path, "");
            return path;
        }

        public Path usePulsar(String version) {
            val path = DIR.resolve("lunastreaming-shell-" + version);
            FileUtils.createDirIfNotExists(path, "");
            return path;
        }

        public boolean cqlshExists() {
            return Files.exists(CQLSH);
        }

        public boolean dsbulkExists(String version) {
            val path = DIR.resolve("dsbulk-" + version);
            return Files.exists(path);
        }

        public boolean pulsarExists(String version) {
            val path = DIR.resolve("lunastreaming-shell-" + version);
            return Files.exists(path);
        }
    }

    public boolean exists() {
        return Files.exists(DIR);
    }

    private Path resolveDefaultAstraHomeFolder() {
        return CliProperties.defaultHomeFolder(isWindows);
    }
}
