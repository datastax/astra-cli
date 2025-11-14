package com.dtsx.astra.cli.commands.role;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.mixins.HelpMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

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
public class RoleCmd {
    @Mixin
    public HelpMixin help;
}
