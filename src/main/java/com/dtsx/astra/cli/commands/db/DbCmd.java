package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.commands.db.collections.CollectionListCmd;
import com.dtsx.astra.cli.commands.db.keyspace.KeyspaceListCmd;
import picocli.CommandLine.Command;

@Command(
    name = "db",
    subcommands = {
        DbListCmd.class,
        DbGetCmd.class,
        DbCreateCmd.class,
        DbResumeCmd.class,
        KeyspaceListCmd.class,
        CollectionListCmd.class,
    }
)
public class DbCmd extends DbListImpl {}
