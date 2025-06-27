package com.dtsx.astra.cli.commands.user;

import picocli.CommandLine.Command;

@Command(
    name = "user",
    description = "Manage users in your organization",
    subcommands = {
        UserListCmd.class,
        UserGetCmd.class,
        UserInviteCmd.class,
        UserDeleteCmd.class
    }
)
public final class UserCmd extends UserListImpl {}