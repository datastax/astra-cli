package com.dtsx.astra.cli.commands.config.home;

import com.dtsx.astra.cli.core.mixins.HelpMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "home",
    description = "All things related to the Astra CLI home folder",
    subcommands = {
        ConfigHomePathCmd.class,
    }
)
public class ConfigHomeCmd {
    @Mixin
    public HelpMixin help;
}
