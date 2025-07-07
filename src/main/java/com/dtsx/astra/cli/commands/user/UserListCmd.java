package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "list",
    description = "List all users in the org"
)
@Example(
    comment = "List all users in your organization",
    command = "astra user list"
)
public final class UserListCmd extends UserListImpl {}
