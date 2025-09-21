package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.config.home.ConfigHomeCmd;
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
        ConfigPathCmd.class,
        ConfigHomeCmd.class,
    }
)
@Example(
    command = "${cli.name} config",
    comment = "List your Astra CLI profiles."
)
@Example(
    command = "${cli.name} config create --token @token.txt --name prod",
    comment = "Create a new Astra CLI profile."
)
public final class ConfigCmd extends ConfigListImpl {}
