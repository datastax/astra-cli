package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.AstraConfigFileMixin;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.output.*;
import com.dtsx.astra.cli.operations.Operation;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import static picocli.CommandLine.Help.Ansi.OFF;

@Command(
    version = AstraCli.VERSION,
    mixinStandardHelpOptions = true,
    commandListHeading = "%nCommands:%n",
    footer = "%nSee 'astra <command> --help' for more information on a specific command."
)
public abstract class AbstractCmd<OpRes> implements Runnable {
    public static final String DEFAULT_VALUE = "  @|faint (default: |@@|faint,italic ${DEFAULT-VALUE}|@@|faint )|@";

    @Spec
    protected CommandSpec spec;

    @Mixin
    private AstraColors.Mixin csMixin;

    @Mixin
    private AstraConfigFileMixin cfgMixin;

    @Mixin
    private OutputType.Mixin outputTypeMixin;

    @Mixin
    private AstraLogger.Mixin loggerMixin;

    protected OutputAll execute(OpRes _result) {
        throw new UnsupportedOperationException("Operation does not supporting outputting in the '" + OutputType.requested().name().toLowerCase() + "' format");
    }

    protected OutputHuman executeHuman(OpRes _result) {
        throw new UnsupportedOperationException();
    }

    protected OutputJson executeJson(OpRes _result) {
        throw new UnsupportedOperationException();
    }

    protected OutputCsv executeCsv(OpRes _result) {
        throw new UnsupportedOperationException();
    }

    protected abstract Operation<OpRes> mkOperation();

    @MustBeInvokedByOverriders
    protected void prelude() {
        spec.commandLine().setColorScheme(csMixin.getColorScheme());
    }

    @MustBeInvokedByOverriders
    protected void postlude() {}

    @Override
    public final void run() {
        if (OutputType.isNotHuman()) {
            AstraColors.setAnsi(OFF);
        }

        this.prelude();
        val result = evokeProperExecuteFunction();
        this.postlude();

        if (!result.isEmpty()) {
            val formatted = AstraConsole.format(result);

            if (formatted.stripTrailing().endsWith("\n")) {
                AstraConsole.getOut().print(formatted);
            } else {
                AstraConsole.getOut().println(formatted);
            }
        }
    }

    private String evokeProperExecuteFunction() {
        val result = mkOperation().execute();

        try {
            return switch (OutputType.requested()) {
                case HUMAN -> executeHuman(result).renderAsHuman();
                case JSON -> executeJson(result).renderAsJson();
                case CSV -> executeCsv(result).renderAsCsv();
            };
        } catch (UnsupportedOperationException e) {
            return execute(result).render(OutputType.requested());
        }
    }

    protected final AstraConfig config() {
        return cfgMixin.getAstraConfig();
    }
}
