package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "config",
    description = "Manage your Astra CLI configuration profiles.",
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
    comment = "List your Astra CLI profiles."
)
public final class ConfigCmd extends ConfigListImpl {}
