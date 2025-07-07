package com.dtsx.astra.cli.config;

import com.dtsx.astra.cli.utils.FileUtils;
import lombok.val;

import java.io.File;

public class AstraHome {
    public static final File DIR = new File(System.getProperty("user.home") + File.separator + ".astra");

    public static class Dirs {
        private static final File SCB = new File(DIR, "scb");
        private static final File COMPLETIONS_CACHE = new File(DIR, "completions-cache");
        private static final File LOGS = new File(DIR, "logs");
        private static final File CQLSH = new File(DIR, "cqlsh-astra");

        public static File useScb() {
            FileUtils.createDirIfNotExists(SCB, "");
            return SCB;
        }

        public static File useCompletionsCache() {
            FileUtils.createDirIfNotExists(COMPLETIONS_CACHE, "");
            return COMPLETIONS_CACHE;
        }

        public static File useLogs() {
            FileUtils.createDirIfNotExists(LOGS, "");
            return LOGS;
        }

        public static File useCqlsh() {
            FileUtils.createDirIfNotExists(CQLSH, "");
            return CQLSH;
        }

        public static File useDsbulk(String version) {
            val file = new File(DIR, "dsbulk-" + version);
            FileUtils.createDirIfNotExists(file, "");
            return file;
        }

        public static File usePulsar(String version) {
            val file = new File(DIR, "lunastreaming-shell-" + version);
            FileUtils.createDirIfNotExists(file, "");
            return file;
        }
    }

    public static boolean exists() {
        return DIR.exists();
    }
}
