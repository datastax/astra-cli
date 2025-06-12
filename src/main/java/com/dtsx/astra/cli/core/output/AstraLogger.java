package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.cli.config.AstraHome;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class AstraLogger {
    @Getter
    private static Level level = Level.REGULAR;
    private static LoadingSpinner globalSpinner;

    private static final List<String> accumulated = Collections.synchronizedList(new ArrayList<>());

    public static String useSessionLogFilePath() {
        return useSessionLogFile().getAbsolutePath();
    }

    private static File useSessionLogFile() {
        return new File(AstraHome.Dirs.useLogs(), Instant.now().toString().replace(":", "-") + ".log");
    }

    enum Level {
        QUIET,
        REGULAR,
        VERBOSE,
    }

    public static class Mixin {
        @Option(names = { "-v", "--verbose" }, description = "Enable verbose logging output")
        public void setVerbose(boolean verbose) {
            if (verbose) {
                level = Level.VERBOSE;
            } else {
                level = Level.REGULAR;
            }
        }

        @Option(names = { "-q", "--quiet" }, description = "Suppress informational output")
        public void setQuiet(boolean quiet) {
            if (quiet) {
                level = Level.QUIET;
            } else {
                level = Level.REGULAR;
            }
        }
    }

    public static void debug(String... msg) {
        append(AstraColors.NEUTRAL_300.use("[DEBUG] ") + String.join("", msg), Level.VERBOSE);
    }

    public static void info(String... msg) {
        append(AstraColors.CYAN_600.use("[INFO] ") + String.join("", msg), Level.REGULAR);
    }

    public static void warn(String... msg) {
        append(AstraColors.ORANGE_400.use("[WARN] ") + String.join("", msg), Level.REGULAR);
    }

    public static void started(String... msg) {
        append("@|green [STARTED]|@ " + String.join("", msg), Level.VERBOSE);
    }

    public static void done(String... msg) {
        append("@|green [DONE]|@ " + String.join("", msg), Level.VERBOSE);
    }

    public static <T> T loading(@NonNull String initialMsg, Function<Consumer<String>, T> supplier) {
//        accumulated.add("[LOADING:STARTED] " + initialMsg);

        started(initialMsg);

        boolean isFirstLoading = globalSpinner == null;
        
        if (isFirstLoading) {
            globalSpinner = new LoadingSpinner(initialMsg);
            globalSpinner.start();
        } else {
            globalSpinner.pushMessage(initialMsg);
        }

        try {
            return supplier.apply((msg) -> {
                globalSpinner.updateMessage(msg);
                accumulated.add("[LOADING:UPDATED] " + msg);
            });
        } finally {
            if (isFirstLoading) {
                globalSpinner.stop();
                globalSpinner = null;
            } else {
                globalSpinner.popMessage();
            }
            done(initialMsg);
        }
    }

    public static void exception(String... msg) {
        append(AstraColors.RED_500.use("[ERROR] ") + String.join("", msg), Level.VERBOSE);
    }

    public static <E extends Exception> E exception(E e) {
        val sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        exception(sw.toString());
        return e;
    }

    public static void dumpLogs() {
        try (var writer = new FileWriter(useSessionLogFile())) {
            for (String line : accumulated) {
                writer.write(AstraColors.stripAnsi(line));
                writer.write(System.lineSeparator());
            }
        } catch (Exception _) {}
    }

    private static void append(String msg, Level minLevel) {
        accumulated.add(msg);

        if (level.ordinal() < minLevel.ordinal()) {
            return;
        }

        if (globalSpinner != null) {
            globalSpinner.pause();
        }

        try {
            AstraConsole.errorln(msg);
        } finally {
            if (globalSpinner != null) {
                globalSpinner.resume();
            }
        }
    }
}
