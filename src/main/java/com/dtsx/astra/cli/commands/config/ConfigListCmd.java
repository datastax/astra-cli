package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "list",
    description = "List your Astra CLI configurations."
)
@Example(
    comment = "List your Astra CLI configurations.",
    command = "${cli.name} config list"
)
@Example(
    comment = "List your Astra CLI configurations for a specific Astra environment.",
    command = "${cli.name} config list --env dev"
)
public final class ConfigListCmd extends ConfigListImpl {}
