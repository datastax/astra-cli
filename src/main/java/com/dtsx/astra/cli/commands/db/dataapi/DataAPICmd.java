package com.dtsx.astra.cli.commands.db.dataapi;

import com.dtsx.astra.cli.core.mixins.HelpMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "data-api",
    description = "Work with the Data API",
    subcommands = {
        DataAPIReplCmd.class,
        DataAPIExecCmd.class,
    }
)
public class DataAPICmd {
    @Mixin
    public HelpMixin help;
}
