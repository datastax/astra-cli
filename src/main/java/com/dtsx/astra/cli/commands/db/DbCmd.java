package com.dtsx.astra.cli.commands.db;

import picocli.CommandLine.Command;

@Command(
    name = "db",
    subcommands = {
        DbListCmd.class,
        DbGetCmd.class
    }
)
public class DbCmd extends DbListImpl {}
