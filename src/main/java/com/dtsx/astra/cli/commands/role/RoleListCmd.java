package com.dtsx.astra.cli.commands.role;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "list",
    description = "Display all roles"
)
@Example(
    comment = "List all available roles in your organization",
    command = "${cli.name} role list"
)
public final class RoleListCmd extends RoleListImpl {}
