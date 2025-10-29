package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "user",
    description = "Manage users in your organization",
    subcommands = {
        UserListCmd.class,
        UserGetCmd.class,
        UserInviteCmd.class,
        UserDeleteCmd.class,
    }
)
@Example(
    comment = "List all your Astra users",
    command = "${cli.name} user"
)
@AliasForSubcommand(UserListCmd.class)
public final class UserCmd extends UserListImpl {}
