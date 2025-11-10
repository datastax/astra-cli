package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.config.home.ConfigHomeCmd;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "config",
    description = "Manage your Astra CLI configuration profiles",
    subcommands = {
        ConfigListCmd.class,
        ConfigCreateCmd.class,
        ConfigGetCmd.class,
        ConfigDeleteCmd.class,
        ConfigRenameCmd.class,
        ConfigUseCmd.class,
        ConfigPathCmd.class,
        ConfigHomeCmd.class,
    }
)
@Example(
    comment = "List your configuration profiles",
    command = "${cli.name} config"
)
@Example(
    comment = "Interactively create a new configuration profile",
    command = "${cli.name} config setup"
)
@Example(
    comment = "Programmatically create a new configuration profile",
    command = "${cli.name} config create my_profile --token @token.txt"
)
@Example(
    comment = "Set an existing configuration profile as default",
    command = "${cli.name} config use my_profile"
)
@AliasForSubcommand(ConfigListCmd.class)
public final class ConfigCmd extends ConfigListImpl {}
