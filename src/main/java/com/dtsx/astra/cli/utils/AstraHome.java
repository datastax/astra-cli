package com.dtsx.astra.cli.utils;

import java.io.File;

public class AstraHome {
    public static final File DIR = new File(System.getProperty("user.home") + File.separator + ".astra");

    public static class Dirs {
        public static final File SCB = new File(DIR, "scb");
        public static final File COMPLETIONS_CACHE = new File(DIR, "completions-cache");
        public static final File LOGS = new File(DIR, "logs");
    }

    public static boolean exists() {
        return DIR.exists();
    }
}
