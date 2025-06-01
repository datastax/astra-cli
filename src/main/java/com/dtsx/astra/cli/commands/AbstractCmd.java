package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.AstraConfigFileMixin;
import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.output.AstraConsole;
import com.dtsx.astra.cli.output.output.OutputAll;
import com.dtsx.astra.cli.output.output.OutputCsv;
import com.dtsx.astra.cli.output.output.OutputJson;
import com.dtsx.astra.cli.output.output.OutputType;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import static picocli.CommandLine.Help.Ansi.OFF;

@Command(
    version = AstraCli.VERSION,
    subcommands = {
        CommandLine.HelpCommand.class,
    }
)
public abstract class AbstractCmd implements Runnable {
    @Spec
    protected CommandSpec spec;

    @Option(names = { "--help", "-h" }, usageHelp = true)
    public boolean helpRequested;

    @Option(names = { "--version", "-v" }, versionHelp = true)
    public boolean versionRequested;

    @Mixin
    private AstraColors.Mixin csMixin;

    @Mixin
    private AstraConfigFileMixin cfgMixin;

    @Mixin
    private OutputType.Mixin outputMixin;

    protected OutputAll execute() {
        return null;
    }

    protected String executeHuman() {
        throw new UnsupportedOperationException("Operation does not supporting outputting in 'human' format");
    }

    protected OutputJson executeJson() {
        throw new UnsupportedOperationException("Operation does not supporting outputting in 'json' format");
    }

    protected OutputCsv executeCsv() {
        throw new UnsupportedOperationException("Operation does not supporting outputting in 'csv' format");
    }

    @MustBeInvokedByOverriders
    protected void prelude() {
        spec.commandLine().setColorScheme(csMixin.getColorScheme());
    }

    @MustBeInvokedByOverriders
    protected void postlude() {}

    @Override
    public void run() {
        if (OutputType.isNotHuman()) {
            AstraColors.setAnsi(OFF);
        }

        this.prelude();
        val result = evokeProperExecuteFunction();
        this.postlude();

        AstraConsole.print(switch (OutputType.requested()) {
            case HUMAN -> result.renderAsHuman();
            case JSON -> result.renderAsJson();
            case CSV -> result.renderAsCsv();
        });
    }

    private OutputAll evokeProperExecuteFunction() {
        val executeResult = execute();

        if (executeResult == null) {
            return new OutputAll() {
                @Override
                public String renderAsHuman() {
                    return executeHuman();
                }

                @Override
                public String renderAsJson() {
                    return executeJson().renderAsJson();
                }

                @Override
                public String renderAsCsv() {
                    return executeCsv().renderAsCsv();
                }
            };
        }

        return executeResult;
    }

    protected AstraConfig config() {
        return cfgMixin.getAstraConfig();
    }
}
