package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.commands.db.collections.CollectionCreateCmd;
import com.dtsx.astra.cli.commands.db.collections.CollectionDeleteCmd;
import com.dtsx.astra.cli.commands.db.collections.CollectionListCmd;
import com.dtsx.astra.cli.commands.db.keyspace.KeyspaceCreateCmd;
import com.dtsx.astra.cli.commands.db.keyspace.KeyspaceDeleteCmd;
import com.dtsx.astra.cli.commands.db.keyspace.KeyspaceListCmd;
import picocli.CommandLine.Command;

@Command(
    name = "db",
    subcommands = {
        DbListCmd.class,
        DbGetCmd.class,
        DbCreateCmd.class,
        DbCreateDotEnv.class,
        DbResumeCmd.class,
        KeyspaceListCmd.class,
        KeyspaceCreateCmd.class,
        KeyspaceDeleteCmd.class,
        CollectionListCmd.class,
        CollectionCreateCmd.class,
        CollectionDeleteCmd.class,
    }
)
public class DbCmd extends DbListImpl {}
