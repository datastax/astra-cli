package com.dtsx.astra.cli.commands.role;

import com.dtsx.astra.cli.commands.user.AbstractCmd;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand.None;
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
@AliasForSubcommand(None.class)
public class RoleCmd extends AbstractCmd {}
