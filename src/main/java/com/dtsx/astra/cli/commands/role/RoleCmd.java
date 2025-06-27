package com.dtsx.astra.cli.commands.role;

import picocli.CommandLine.Command;

@Command(
    name = "role",
    description = "Manage roles in your organization",
    subcommands = {
        RoleListCmd.class,
        RoleGetCmd.class
    }
)
public class RoleCmd {}
