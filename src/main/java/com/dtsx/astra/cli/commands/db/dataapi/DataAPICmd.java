package com.dtsx.astra.cli.commands.db.dataapi;

import com.dtsx.astra.cli.core.mixins.HelpMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "data-api",
    description = "Easily work with the Data API @|italic (beta)|@",
    subcommands = {
        DataAPIReplCmd.class,
        DataAPIExecCmd.class,
    }
)
public class DataAPICmd {
    @Mixin
    public HelpMixin help;
}
