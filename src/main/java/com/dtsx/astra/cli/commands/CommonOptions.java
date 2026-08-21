package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.completions.impls.HelpAllCompletion;
import com.dtsx.astra.cli.core.completions.impls.OutputTypeCompletion;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.Optional;

import static com.dtsx.astra.cli.commands.AbstractOperationalCmd.SHOW_CUSTOM_DEFAULT;

@AllArgsConstructor
@NoArgsConstructor
public class CommonOptions {
    public static CommonOptions EMPTY = new CommonOptions();

    public enum HelpLevel { NONE, STANDARD, ALL }

    @Option(
        names = { "-h", "--help" },
        description = "Show this help message and exit.",
        fallbackValue = "standard",
        completionCandidates = HelpAllCompletion.class,
        paramLabel = "all",
        arity = "0..1",
        help = true
    )
    private Optional<String> helpRequested = Optional.empty();

    @Option(
        names = { "--help-all" },
        description = "Show this help message and exit.",
        help = true,
        hidden = true
    )
    private Optional<Boolean> helpAllRequested = Optional.empty();

    public enum ColorMode { auto, never, always }

    private Optional<Ansi> ansi = Optional.empty();

    @Option(
        names = "--color",
        description = { "One of: ${COMPLETION-CANDIDATES}", SHOW_CUSTOM_DEFAULT + "auto" },
        arity = "0..1",
        fallbackValue = "always",
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
        hidden = true
    )
    private void setAnsi(boolean noColor) {
        this.ansi = Optional.of((noColor) ? Ansi.OFF : Ansi.ON);
    }

    @Option(
        names = { "--output", "-o" },
        completionCandidates = OutputTypeCompletion.class,
        description = { "One of: ${COMPLETION-CANDIDATES}", SHOW_CUSTOM_DEFAULT + "human" },
        paramLabel = "FORMAT"
    )
    private Optional<OutputType> outputType = Optional.empty();

    @Option(
        names = { "-V", "--verbose" },
        description = "Enable verbose logging output",
        showDefaultValue = Visibility.NEVER
    )
    private Optional<Boolean> verbose = Optional.empty();

    @Option(
        names = { "-q", "--quiet" },
        description = "Only output essential information",
        showDefaultValue = Visibility.NEVER
    )
    private Optional<Boolean> quiet = Optional.empty();

    @Option(
        names = { "--spinner" },
        description = { "Enable/disable loading spinners", SHOW_CUSTOM_DEFAULT + "enabled if tty and not quiet" },
        negatable = true,
        fallbackValue = "true"
    )
    private Optional<Boolean> enableSpinner = Optional.empty();

    private Optional<Boolean> shouldDumpLogs = Optional.empty();

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
                shouldDumpLogs = Optional.of(false);
                dumpLogsTo = Optional.empty();
            } else {
                shouldDumpLogs = Optional.of(true);

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
        shouldDumpLogs = Optional.of(!noDumpLogs);
        dumpLogsTo = Optional.empty();
    }

    @Option(
        names = "--no-input",
        description = "Don't ask for user input (e.g. confirmation prompts)",
        showDefaultValue = Visibility.NEVER
    )
    private Optional<Boolean> noInput = Optional.empty();

    public Optional<Ansi> ansi() {
        return ansi;
    }

    public OutputType outputType() {
        return outputType.orElse(OutputType.HUMAN);
    }

    public boolean verbose() {
        return verbose.orElse(false);
    }

    public boolean quiet() {
        return quiet.orElse(false);
    }

    public Optional<Boolean> enableSpinner() {
        return enableSpinner;
    }

    public boolean shouldDumpLogs() {
        return shouldDumpLogs.orElse(false);
    }

    public Optional<Path> dumpLogsTo() {
        return dumpLogsTo;
    }

    public boolean noInput() {
        return noInput.orElse(false);
    }

    public HelpLevel helpLevel() {
        if (helpAllRequested.isPresent() && helpAllRequested.get()) {
            return HelpLevel.ALL;
        }

        if (helpRequested.isEmpty()) {
            return HelpLevel.NONE;
        }

        if (helpRequested.get().equalsIgnoreCase(HelpLevel.ALL.name())) {
            return HelpLevel.ALL;
        }

        return HelpLevel.STANDARD;
    }

    public boolean helpRequested() {
        return helpLevel() != HelpLevel.NONE;
    }

    public CommonOptions merge(CommonOptions other) {
        if (this == EMPTY) {
            return other;
        }
        if (other == EMPTY) {
            return this;
        }
        return new CommonOptions(
            (this.helpRequested.isPresent() ? this.helpRequested : other.helpRequested),
            (this.helpAllRequested.isPresent()) ? this.helpAllRequested : other.helpAllRequested,
            (this.ansi.isPresent()) ? this.ansi : other.ansi,
            (this.outputType.isPresent()) ? this.outputType : other.outputType,
            (this.verbose.isPresent()) ? this.verbose : other.verbose,
            (this.quiet.isPresent()) ? this.quiet : other.quiet,
            (this.enableSpinner.isPresent()) ? this.enableSpinner : other.enableSpinner,
            (this.shouldDumpLogs.isPresent()) ? this.shouldDumpLogs : other.shouldDumpLogs,
            (this.dumpLogsTo.isPresent()) ? this.dumpLogsTo : other.dumpLogsTo,
            (this.noInput.isPresent()) ? this.noInput : other.noInput
        );
    }
}
