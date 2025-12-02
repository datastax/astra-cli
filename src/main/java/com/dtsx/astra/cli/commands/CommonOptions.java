package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.completions.impls.OutputTypeCompletion;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.mixins.HelpMixin;
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
public class CommonOptions extends HelpMixin { // I don't like extending here but mixins don't compose w/ arg groups :(
    @Getter
    private Optional<Ansi> ansi = Optional.empty();

    public enum ColorMode { auto, never, always }

    @Option(
        names = "--color",
        description = { "One of: ${COMPLETION-CANDIDATES}", SHOW_CUSTOM_DEFAULT + "auto" },
        arity = "0..1",
        fallbackValue = "always",
        defaultValue = "${ASTRA_DEFAULT_COLOR:-auto}",
        paramLabel = "WHEN"
    )
    private void setAnsi(ColorMode mode) {
        switch (mode) {
            case always -> this.ansi = Optional.of(Ansi.ON); // auto is handled by Optional.empty() here, not Ansi.AUTO
            case never -> this.ansi = Optional.of(Ansi.OFF);
            default -> this.ansi = Optional.empty();
        }
    }

    @Option(
        names = "--no-color",
        defaultValue = "${ASTRA_NO_COLOR:-false}", // no DEFAULT in env var on purpose to better adhere to per-program NO_COLOR semantics
        hidden = true
    )
    private void setAnsi(boolean noColor) {
        this.ansi = Optional.of((noColor) ? Ansi.OFF : Ansi.ON);
    }

    @Option(
        names = { "--output", "-o" },
        completionCandidates = OutputTypeCompletion.class,
        defaultValue = "${ASTRA_DEFAULT_OUTPUT_TYPE:-human}",
        description = "One of: ${COMPLETION-CANDIDATES}",
        paramLabel = "FORMAT"
    )
    @Getter
    private OutputType outputType;

    @Getter
    @Option(
        names = { "-V", "--verbose" },
        description = "Enable verbose logging output",
        defaultValue = "${ASTRA_DEFAULT_VERBOSE:-false}",
        showDefaultValue = Visibility.NEVER
    )
    private boolean verbose;

    @Getter
    @Option(
        names = { "-q", "--quiet" },
        description = "Only output essential information",
        defaultValue = "${ASTRA_DEFAULT_QUIET:-false}",
        showDefaultValue = Visibility.NEVER
    )
    private boolean quiet;

    @Getter
    @Option(
        names = { "--spinner" },
        description = { "Enable/disable loading spinners", SHOW_CUSTOM_DEFAULT + "enabled if tty and not quiet" },
        defaultValue = "${ASTRA_DEFAULT_SPINNER:-" + Option.NULL_VALUE + "}",
        negatable = true,
        fallbackValue = "true"
    )
    private Optional<Boolean> enableSpinner;

    @Getter
    private boolean shouldDumpLogs = false;

    @Getter
    private Optional<Path> dumpLogsTo = Optional.empty();

    @Option(
        names = "--dump-logs",
        description = { "Write all logs to an optionally specified file", SHOW_CUSTOM_DEFAULT + "${cli.home-folder.path}/logs/<file>.log" },
        fallbackValue = "__fallback__",
        defaultValue = "${ASTRA_DEFAULT_DUMP_LOGS:-" + Option.NULL_VALUE + "}",
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
        description = "Don't ask for user input (e.g. confirmation prompts)",
        defaultValue = "${ASTRA_DEFAULT_NO_INPUT:-false}",
        showDefaultValue = Visibility.NEVER
    )
    private boolean noInput;
}
