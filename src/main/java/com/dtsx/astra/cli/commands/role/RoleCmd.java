package com.dtsx.astra.cli.commands.role;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "role",
    description = "Discover Astra roles",
    subcommands = {
        RoleListCmd.class,
        RoleGetCmd.class,
    }
)
@Example(
    comment = "List all available roles",
    command = "${cli.name} role list"
)
@Example(
    comment = "Get details for a specific role",
    command = "${cli.name} role get \"Database Administrator\""
)
public class RoleCmd {}
