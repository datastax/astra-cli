package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "list",
    description = "List your Astra CLI configurations."
)
@Example(
    command = "astra config list",
    comment = "List your Astra CLI configurations."
)
public final class ConfigListCmd extends ConfigListImpl {}
