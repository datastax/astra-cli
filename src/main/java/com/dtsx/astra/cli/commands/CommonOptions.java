package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.completions.impls.OutputTypeCompletion;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import lombok.Getter;
import lombok.experimental.Accessors;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.Optional;

import static com.dtsx.astra.cli.commands.AbstractCmd.SHOW_CUSTOM_DEFAULT;

@Accessors(fluent = true)
public class CommonOptions {
    @Option(
        names = { "-h", "--help" },
        description = "Show this help message and exit.",
        showDefaultValue = Visibility.NEVER,
        usageHelp = true
    )
    private boolean helpRequested;

    @Getter
    private Optional<Ansi> ansi = Optional.empty();

    enum ColorMode { auto, never, always }

    @Option(
        names = "--color",
        description = { "One of: ${COMPLETION-CANDIDATES}", SHOW_CUSTOM_DEFAULT + "auto" },
        paramLabel = "WHEN"
    )
    private void setAnsi(ColorMode mode) {
        switch (mode) {
            case always -> this.ansi = Optional.of(Ansi.ON); // auto is handled by Optional.empty() here, not Ansi.AUTO
            case never -> this.ansi = Optional.of(Ansi.OFF);
        }
    }

    @Option(
        names = "--no-color",
        hidden = true
    )
    private void setAnsi(boolean noColor) {
        if (noColor) {
            this.ansi = Optional.of(Ansi.OFF);
        } else {
            throw new OptionValidationException("--no-color", "--no-color must be called without a value (or with 'true'); use --color=never instead");
        }
    }

    @Option(
        names = { "--output", "-o" },
        completionCandidates = OutputTypeCompletion.class,
        defaultValue = "human",
        description = "One of: ${COMPLETION-CANDIDATES}",
        paramLabel = "FORMAT"
    )
    @Getter
    private OutputType outputType;

    @Getter
    @Option(
        names = { "-V", "--verbose" },
        description = "Enable verbose logging output",
        showDefaultValue = Visibility.NEVER
    )
    private boolean verbose;

    @Getter
    @Option(
        names = { "-q", "--quiet" },
        description = "Suppress informational output",
        showDefaultValue = Visibility.NEVER
    )
    private boolean quiet;

    @Getter
    @Option(
        names = { "--spinner" },
        description = "Enable/disable the loading spinner",
        negatable = true,
        defaultValue = "${cli.output.spinner.default}",
        showDefaultValue = Visibility.NEVER,
        fallbackValue = "true"
    )
    private boolean enableSpinner;

    @Getter
    private boolean shouldDumpLogs = false;

    @Getter
    private Optional<Path> dumpLogsTo = Optional.empty();

    @Option(
        names = "--dump-logs",
        description = { "Write all logs to an optionally specified file", SHOW_CUSTOM_DEFAULT + "${cli.home-folder.path}/logs/<file>.log" },
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

                if (!path.toString().equalsIgnoreCase("__fallback__")) {
                    dumpLogsTo = Optional.of(path);
                }
            }
        });
    }

    @Option(
        names = "--no-dump-logs",
        hidden = true
    )
    private void setDumpLogs(boolean noDumpLogs) {
        if (noDumpLogs) {
            shouldDumpLogs = false;
        } else {
            throw new OptionValidationException("--no-dump-logs", "--no-dump-logs must be called without a value (or with 'true'); use --dump-logs[=FILE] instead");
        }
    }

    @Getter
    @Option(
        names = "--no-input",
        description = "Never ask for user input (e.g. confirmation prompts)",
        showDefaultValue = Visibility.NEVER
    )
    private boolean noInput;
}
