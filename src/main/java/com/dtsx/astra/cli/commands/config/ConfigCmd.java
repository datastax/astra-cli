package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "config",
    description = "List your Astra CLI configurations.",
    subcommands = {
        ConfigListCmd.class,
        ConfigCreateCmd.class,
        ConfigGetCmd.class,
        ConfigDeleteCmd.class,
        ConfigUseCmd.class,
    }
)
@Example(
    command = "astra config",
    comment = "List your Astra CLI configurations."
)
public final class ConfigCmd extends ConfigListImpl {}
