package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CliProperties;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.val;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.commands.AbstractCmd.DEFAULT_END;
import static com.dtsx.astra.cli.commands.AbstractCmd.DEFAULT_START;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Accessors(fluent = true)
public class AstraLogger {
    @Getter
    private final Level level;
    private final Supplier<CliContext> ctxSupplier;

    private final boolean shouldDumpLogs;
    private final Supplier<Path> sessionLogFile;

    private final List<String> accumulated = Collections.synchronizedList(new ArrayList<>());

    private LoadingSpinner globalSpinner;

    public AstraLogger(Level level, Supplier<CliContext> ctxSupplier, boolean shouldDumpLogs, Optional<Path> dumpLogsTo) {
        this.level = level;
        this.ctxSupplier = ctxSupplier;
        this.shouldDumpLogs = shouldDumpLogs;

        if (dumpLogsTo.isPresent()) {
            this.sessionLogFile = dumpLogsTo::get;

            info("Dumping logs to '", dumpLogsTo.get().toString(), "' at the end of this command.");
        } else {
            val cachedLogFile = new Object() {
                Path ref = null;
            };

            this.sessionLogFile = () -> {
                if (cachedLogFile.ref == null) {
                    val timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").format(Instant.now().atZone(ZoneId.systemDefault()));
                    cachedLogFile.ref = ctx().home().Dirs.useLogs().resolve(timestamp + ".astra.log");
                }
                return cachedLogFile.ref;
            };
        }
    }

    public String useSessionLogFilePath() {
        return sessionLogFile.get().toString();
    }

    public void banner() {
        val banner = ctx().colors().PURPLE_300.use("""
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

    @Accessors(fluent = true)
    public static class Mixin {
        @Getter
        @Option(
            names = { "-v", "--verbose" },
            description = "Enable verbose logging output"
        )
        private boolean verbose;

        @Getter
        @Option(
            names = { "-q", "--quiet" },
            description = "Suppress informational output"
        )
        private boolean quiet;

        @Getter
        private boolean shouldDumpLogs = false;

        @Getter
        private Optional<Path> dumpLogsTo = Optional.empty();

        @Option(
            names = { "--dump-logs" },
            description = { "Write all logs to an optionally specified file", DEFAULT_START + "${cli.home-folder-path}/logs/<file>.log" + DEFAULT_END },
            fallbackValue = "__fallback__",
            paramLabel = "FILE",
            arity = "0..1"
        )
        private void setDumpLogs(Optional<Path> dest) {
            dest.ifPresent((path) -> {
                if (path.toString().equalsIgnoreCase("false")) {
                    shouldDumpLogs = false;
                    dumpLogsTo = Optional.empty();
                } else {
                    shouldDumpLogs = true;

                    if (path.toString().equalsIgnoreCase("__fallback__")) {
                        dumpLogsTo = Optional.of(path);
                    }
                }
            });
        }

        @Option(names = { "--no-dump-logs" }, hidden = true)
        private void setDumpLogs(boolean noDumpLogs) {
            if (noDumpLogs) {
                shouldDumpLogs = false;
            }
        }
    }

    public void debug(String... msg) {
        log(ctx().colors().NEUTRAL_300.use("[DEBUG] ") + String.join("", msg), Level.VERBOSE, true);
    }

    public void info(String... msg) {
        log(ctx().colors().CYAN_600.use("[INFO] ") + String.join("", msg), Level.REGULAR, true);
    }

    public void hint(String... msg) {
        log("@|green [HINT]|@ " + String.join("", msg), Level.REGULAR, true);
    }

    public void warn(String... msg) {
        log(ctx().colors().ORANGE_400.use("[WARNING] ") + String.join("", msg), Level.REGULAR, true);
    }

    public void started(String... msg) {
        log("@|green [STARTED]|@ " + String.join("", msg), Level.VERBOSE, true);
    }

    public void done(String... msg) {
        log("@|green [DONE]|@ " + String.join("", msg), Level.VERBOSE, true);
    }

    public <T> T loading(@NonNull String initialMsg, Function<Consumer<String>, T> supplier) {
        started(initialMsg);

        boolean isFirstLoading = globalSpinner == null;

        if (isFirstLoading) {
            globalSpinner = new LoadingSpinner(initialMsg, ctx());
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

    public void exception(String... msg) {
        log(ctx().colors().RED_500.use("[ERROR] ") + String.join("", msg), Level.VERBOSE, true);
    }

    public <E extends Throwable> E exception(String msg, E e) {
        exception(msg);
        return exception(e);
    }

    public <E extends Throwable> E exception(E e) {
        val sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        exception(sw.toString());
        return e;
    }

    private void log(String msg, Level minLevel, boolean appendToAccumulated) {
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
            ctx().console().errorln(msg);
        } finally {
            if (globalSpinner != null) {
                globalSpinner.resume();
            }
        }
    }

    private boolean logsDumped = false;

    public boolean shouldDumpLogs() {
        return shouldDumpLogs;
    }

    public void dumpLogsToFile() {
        if (logsDumped) {
            return;
        }
        logsDumped = true;

        deleteOldLogs(ctx().home().Dirs.useLogs());

        try (var writer = Files.newBufferedWriter(sessionLogFile.get())) {
            for (String line : accumulated) {
                writer.write(AstraColors.stripAnsi(ctx().console().format(line)));
                writer.write(System.lineSeparator());
            }
        } catch (Exception _) {}
    }

    private void deleteOldLogs(Path logsDir) {
        try {
            @Cleanup val logFiles = Files.list(logsDir);

            logFiles
                .sorted(Comparator.comparing((path) -> {
                    try {
                        return Files.getLastModifiedTime(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .skip(25)
                .forEach((f) -> {
                    try {
                        Files.delete(f);
                    } catch (Exception e) {
                        exception("Error deleting old log file '", f.toString(), "': ", e.getMessage());
                    }
                });
        } catch (Exception _) {}
    }

    private CliContext ctx() {
        return ctxSupplier.get();
    }
}
