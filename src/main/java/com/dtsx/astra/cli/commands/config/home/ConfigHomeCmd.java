package com.dtsx.astra.cli.commands.config.home;

import picocli.CommandLine.Command;

@Command(
    name = "home",
    description = "All things related to the Astra CLI home folder",
    subcommands = {
        ConfigHomePathCmd.class,
    }
)
public class ConfigHomeCmd {}
