package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.core.config.AstraHome;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.commands.AbstractCmd.DEFAULT_END;
import static com.dtsx.astra.cli.commands.AbstractCmd.DEFAULT_START;
import static com.dtsx.astra.cli.core.output.AstraColors.PURPLE_300;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

public class AstraLogger {
    @Getter
    private static Level level = Level.REGULAR;
    private static LoadingSpinner globalSpinner;

    private static File sessionLogFile;
    private static final List<String> accumulated = Collections.synchronizedList(new ArrayList<>());

    private static boolean shouldDumpLogs = false;

    private static File getLogFile() {
        if (sessionLogFile == null) {
            val timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").format(Instant.now().atZone(java.time.ZoneId.systemDefault()));
            sessionLogFile = new File(AstraHome.Dirs.useLogs(), timestamp + ".astra.log");
        }
        return sessionLogFile;
    }

    public static String useSessionLogFilePath() {
        return getLogFile().getAbsolutePath();
    }

    public static void banner() {
        val banner = PURPLE_300.use("""
             _____            __
            /  _  \\   _______/  |_____________
           /  /_\\  \\ /  ___/\\   __\\_  __ \\__  \\
          /    |    \\\\___ \\  |  |  |  | \\ //__ \\_
          \\____|__  /____  > |__|  |__|  (____  /
                  \\/     \\/                   \\/
        
                               Version: %s
        """.stripIndent().formatted(CliProperties.version()));

        log(NL + banner, Level.REGULAR, false);
    }

    public enum Level {
        QUIET,
        REGULAR,
        VERBOSE,
    }

    public static class Mixin {
        @Option(
            names = { "-v", "--verbose" },
            description = "Enable verbose logging output"
        )
        public void setVerbose(boolean verbose) {
            if (verbose) {
                level = Level.VERBOSE;
            } else {
                level = Level.REGULAR;
            }
        }

        @Option(
            names = { "-q", "--quiet" },
            description = "Suppress informational output"
        )
        public void setQuiet(boolean quiet) {
            if (quiet) {
                level = Level.QUIET;
            } else {
                level = Level.REGULAR;
            }
        }

        @Option(
            names = { "--dump-logs" },
            description = { "Write all logs to an optionally specified file", DEFAULT_START + "${cli.home-folder-path}/logs/<file>.log" + DEFAULT_END },
            fallbackValue = "__fallback__",
            paramLabel = "FILE",
            arity = "0..1"
        )
        public void setDumpLogs(Optional<File> dest) {
            dest.ifPresent((file) -> {
                if (file.equals(new File("false"))) {
                    shouldDumpLogs = false;
                    return;
                }

                shouldDumpLogs = true;

                if (!file.equals(new File("__fallback__"))) {
                    sessionLogFile = file;
                }

                AstraLogger.info("Dumping logs to '", getLogFile().getAbsolutePath(), "' at the end of this command.");
            });
        }

        @Option(names = { "--no-dump-logs" }, hidden = true)
        public void setDumpLogs(boolean noDumpLogs) {
            if (noDumpLogs) {
                shouldDumpLogs = false;
            }
        }
    }

    public static void debug(String... msg) {
        log(AstraColors.NEUTRAL_300.use("[DEBUG] ") + String.join("", msg), Level.VERBOSE, true);
    }

    public static void info(String... msg) {
        log(AstraColors.CYAN_600.use("[INFO] ") + String.join("", msg), Level.REGULAR, true);
    }

    public static void warn(String... msg) {
        log(AstraColors.ORANGE_400.use("[WARNING] ") + String.join("", msg), Level.REGULAR, true);
    }

    public static void started(String... msg) {
        log("@|green [STARTED]|@ " + String.join("", msg), Level.VERBOSE, true);
    }

    public static void done(String... msg) {
        log("@|green [DONE]|@ " + String.join("", msg), Level.VERBOSE, true);
    }

    public static <T> T loading(@NonNull String initialMsg, Function<Consumer<String>, T> supplier) {
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
        log(AstraColors.RED_500.use("[ERROR] ") + String.join("", msg), Level.VERBOSE, true);
    }

    public static <E extends Throwable> E exception(E e) {
        val sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        exception(sw.toString());
        return e;
    }

    private static boolean logsDumped = false;

    public static boolean shouldDumpLogs() {
        return shouldDumpLogs;
    }

    public static void dumpLogsToFile() {
        if (logsDumped) {
            return;
        }
        logsDumped = true;

        deleteOldLogs(AstraHome.Dirs.useLogs());

        try (var writer = new FileWriter(getLogFile())) {
            for (String line : accumulated) {
                writer.write(AstraColors.stripAnsi(line));
                writer.write(System.lineSeparator());
            }
        } catch (Exception _) {}
    }

    private static void log(String msg, Level minLevel, boolean appendToAccumulated) {
        if (appendToAccumulated) {
            accumulated.add(msg);
        }

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

    private static void deleteOldLogs(File logsDir) {
        try {
            val logFiles = logsDir.listFiles();

            if (logFiles != null) {
                Stream.of(logFiles)
                    .sorted((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()))
                    .skip(25)
                    .forEach((f) -> {
                        try {
                            Files.delete(f.toPath());
                        } catch (Exception e) {
                            AstraLogger.exception("Error deleting old log file '", f.getAbsolutePath(), "': ", e.getMessage());
                        }
                    });
            }
        } catch (Exception _) {}
    }
}
