package com.dtsx.astra.cli.commands.dotenv;

import com.dtsx.astra.cli.core.mixins.HelpMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "dotenv",
    description = "Easily create and update your .env files",
    subcommands = {
        DotEnvWriteCmd.class,
        DotEnvPrintCmd.class,
        DotEnvListKeysCmd.class,
    }
)
public class DotEnvCmd {
    @Mixin
    public HelpMixin help;
}
