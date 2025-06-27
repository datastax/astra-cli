package com.dtsx.astra.cli.commands.user;

import picocli.CommandLine.Command;

@Command(
    name = "list",
    description = "Display the list of Users in an organization"
)
public final class UserListCmd extends UserListImpl {}