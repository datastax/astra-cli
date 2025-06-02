package com.dtsx.astra.cli.output;

import com.dtsx.astra.cli.exceptions.db.CongratsYouFoundABugException;
import com.dtsx.astra.cli.utils.AstraHome;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.Nullable;
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
    private static volatile LoadingSpinner currentSpinner;

    private static final List<String> accumulated = Collections.synchronizedList(new ArrayList<>());

    private static final File SESSION_LOG_FILE = new File(AstraHome.Dirs.LOGS, Instant.now().toString().replace(":", "-") + ".log");

    public static String getSessionLogFilePath() {
        return SESSION_LOG_FILE.getAbsolutePath();
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

    public static void info(String... msg) {
        append(AstraColors.NEUTRAL_300.use("[INFO] ") + String.join("", msg), Level.VERBOSE, true);
    }

    public static void success(String... msg) {
        append(AstraColors.GREEN_500.use("[OK] ") + String.join("", msg), Level.VERBOSE, true);
    }

    public static void warn(String... msg) {
        append(AstraColors.ORANGE_400.use("[WARN]  ") + String.join("", msg), Level.REGULAR, true);
    }

    public static <T> T loading(@NonNull String initialMsg, @Nullable String doneMsg, Function<Consumer<String>, T> supplier) {
        // Handle nested loading calls
        if (currentSpinner != null) {
            accumulated.add("[LOADING:STARTED] " + initialMsg);
            
            Consumer<String> consumer = (updatedMsg) -> {
                accumulated.add("[LOADING:UPDATED] " + updatedMsg);
            };
            
            try {
                return supplier.apply(consumer);
            } finally {
                if (doneMsg != null) {
                    append("@|green [DONE]|@ " + doneMsg, Level.REGULAR, false);
                    accumulated.add("[LOADING:FINISHED] " + doneMsg);
                } else {
                    accumulated.add("[LOADING:FINISHED] " + initialMsg);
                }
            }
        }
        
        // Normal loading with spinner
        accumulated.add("[LOADING:STARTED] " + initialMsg);

        val spinner = new LoadingSpinner(initialMsg);
        currentSpinner = spinner;

        Consumer<String> consumer = (updatedMsg) -> {
            spinner.updateMessage(updatedMsg);
            accumulated.add("[LOADING:UPDATED] " + updatedMsg);
        };

        spinner.start();
        
        try {
            return supplier.apply(consumer);
        } finally {
            spinner.stop();
            currentSpinner = null;

            if (doneMsg != null) {
                append("@|green [DONE]|@ " + doneMsg, Level.REGULAR, false);
                accumulated.add("[LOADING:FINISHED] " + doneMsg);
            } else {
                accumulated.add("[LOADING:FINISHED] " + initialMsg);
            }
        }
    }

    public static void exception(String... msg) {
        append(AstraColors.RED_500.use("[ERROR] ") + String.join("", msg), Level.VERBOSE, true);
    }

    public static <E extends Exception> E exception(E e) {
        val sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        exception(sw.toString());
        return e;
    }

    public static void dumpLogs() {
        try (var writer = new FileWriter(SESSION_LOG_FILE)) {
            for (String line : accumulated) {
                writer.write(AstraColors.stripAnsi(line));
                writer.write(System.lineSeparator());
            }
        } catch (Exception _) {}
    }

    private static void append(String msg, Level minLevel, boolean appendToAccumulated) {
        if (appendToAccumulated) {
            accumulated.add(msg);
        }

        if (level.ordinal() < minLevel.ordinal()) {
            return;
        }

        LoadingSpinner spinner = currentSpinner;
        if (spinner != null) {
            spinner.pause();
        }

        try {
            AstraConsole.errorln(msg);
        } finally {
            if (spinner != null) {
                spinner.resume();
            }
        }
    }
}
