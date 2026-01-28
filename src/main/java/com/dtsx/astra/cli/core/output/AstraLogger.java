package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.LoadingSpinner.LoadingSpinnerControls;
import com.dtsx.astra.cli.core.properties.CliEnvironment;
import com.dtsx.astra.cli.utils.MiscUtils;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public class AstraLogger {
    @Getter
    private final Level level;
    private final Supplier<CliContext> ctxSupplier;

    private final boolean shouldDumpLogs;
    private final Supplier<Path> sessionLogFile;

    private final List<String> accumulated = Collections.synchronizedList(new ArrayList<>());

    private final Optional<LoadingSpinner> globalSpinner;

    public AstraLogger(Level level, CliEnvironment cliEnv, Supplier<CliContext> ctxSupplier, boolean shouldDumpLogs, Optional<Path> dumpLogsTo, Optional<Boolean> enableSpinner) {
        this.level = level;
        this.ctxSupplier = ctxSupplier;
        this.shouldDumpLogs = shouldDumpLogs;

        val isSpinnerEnabled = enableSpinner.orElseGet(() -> {
            if (System.getProperty("cli.output.spinner.disable", "").equals("true")) { // for tests
                return false;
            }
            return cliEnv.isTty() && level != Level.QUIET;
        });

        if (isSpinnerEnabled) {
            this.globalSpinner = Optional.of(new LoadingSpinner(cliEnv, ctxSupplier));
        } else {
            this.globalSpinner = Optional.empty();
        }

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
                    cachedLogFile.ref = ctx().home().dirs.logs.use().resolve(timestamp + ".astra.log");
                }
                return cachedLogFile.ref;
            };
        }
    }

    public String useSessionLogFilePath() {
        try {
            return sessionLogFile.get().toString();
        } catch (Exception e) {
            return "<unknown>"; // only really triggered in tests if DummyFileSystem is used
        }
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
        """.stripIndent().formatted(ctx().properties().version()));

        log(NL + banner, Level.REGULAR, false);
    }

    public enum Level {
        QUIET,
        REGULAR,
        VERBOSE,
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

    public <T> T loading(@NonNull String rawInitialMsg, Function<Consumer<String>, T> supplier) {
        val initialMsg = ctx().colors().format(rawInitialMsg);

        started(initialMsg);

        val controls = globalSpinner.flatMap(s -> s.start(initialMsg));

        try {
            return supplier.apply((msg) -> {
                controls.ifPresent(c -> c.updateMessage(ctx().colors().format(msg)));
                accumulated.add("[LOADING:UPDATED] " + msg);
            });
        } finally {
            controls.ifPresent(LoadingSpinnerControls::stop);
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
        exception(MiscUtils.captureStackTrace(e));
        return e;
    }

    private void log(String msg, Level minLevel, boolean appendToAccumulated) {
        if (appendToAccumulated) {
            accumulated.add(msg);
        }

        if (level.ordinal() < minLevel.ordinal()) {
            return;
        }

        val resume = globalSpinner.map(LoadingSpinner::pause);

        try {
            ctx().console().errorln(msg);
        } finally {
            resume.ifPresent(Runnable::run);
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

        try (var writer = Files.newBufferedWriter(sessionLogFile.get())) {
            deleteOldLogs(ctx().home().dirs.logs.use());

            for (val line : accumulated) {
                writer.write(AstraColors.stripAnsi(ctx().colors().format(line)));
                writer.write(System.lineSeparator());
            }
        } catch (Exception _) {}
    }

    private void deleteOldLogs(Path logsDir) {
        try {
            @Cleanup val logFiles = Files.list(logsDir);

            logFiles
                .sorted(Comparator.<Path, FileTime>comparing((path) -> {
                    try {
                        return Files.getLastModifiedTime(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).reversed())
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
