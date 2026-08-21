package com.dtsx.astra.cli.commands.config.home;

import com.dtsx.astra.cli.commands.user.AbstractCmd;
import picocli.CommandLine.Command;

@Command(
    name = "home",
    description = "All things related to the Astra CLI home folder",
    subcommands = {
        ConfigHomePathCmd.class,
    }
)
public class ConfigHomeCmd extends AbstractCmd {}
