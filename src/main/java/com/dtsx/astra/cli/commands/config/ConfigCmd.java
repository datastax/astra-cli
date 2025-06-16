package com.dtsx.astra.cli.commands.config;

import picocli.CommandLine.Command;

@Command(
    name = "config",
    subcommands = {
        ConfigListCmd.class,
        ConfigCreateCmd.class,
        ConfigGetCmd.class,
        ConfigDeleteCmd.class,
        ConfigUseCmd.class
    }
)
public final class ConfigCmd extends ConfigListImpl {}
