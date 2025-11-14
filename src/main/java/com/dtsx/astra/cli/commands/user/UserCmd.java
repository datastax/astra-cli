package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.mixins.HelpMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

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
    command = "${cli.name} user list"
)
public final class UserCmd {
    @Mixin
    public HelpMixin help;
}
