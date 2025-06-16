package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.commands.db.collections.CollectionCreateCmd;
import com.dtsx.astra.cli.commands.db.collections.CollectionDeleteCmd;
import com.dtsx.astra.cli.commands.db.collections.CollectionListCmd;
import com.dtsx.astra.cli.commands.db.endpoints.EndpointApiCmd;
import com.dtsx.astra.cli.commands.db.endpoints.EndpointPlaygroundCmd;
import com.dtsx.astra.cli.commands.db.endpoints.EndpointSwaggerCmd;
import com.dtsx.astra.cli.commands.db.keyspace.KeyspaceCreateCmd;
import com.dtsx.astra.cli.commands.db.keyspace.KeyspaceDeleteCmd;
import com.dtsx.astra.cli.commands.db.keyspace.KeyspaceListCmd;
import com.dtsx.astra.cli.commands.db.misc.CloudsListCmd;
import com.dtsx.astra.cli.commands.db.region.RegionListClassicCmd;
import com.dtsx.astra.cli.commands.db.region.RegionListCmd;
import com.dtsx.astra.cli.commands.db.region.RegionListServerlessCmd;
import com.dtsx.astra.cli.commands.db.region.RegionListVectorCmd;
import picocli.CommandLine.Command;

@Command(
    name = "db",
    subcommands = {
        DbListCmd.class,
        DbGetCmd.class,
        DbCreateCmd.class,
        DbDeleteCmd.class,
        DbCreateDotEnv.class,
        DbResumeCmd.class,
        KeyspaceListCmd.class,
        KeyspaceCreateCmd.class,
        KeyspaceDeleteCmd.class,
        CollectionListCmd.class,
        CollectionCreateCmd.class,
        CollectionDeleteCmd.class,
        RegionListClassicCmd.class,
        RegionListServerlessCmd.class,
        RegionListVectorCmd.class,
        RegionListCmd.class,
        CloudsListCmd.class,
        EndpointSwaggerCmd.class,
        EndpointApiCmd.class,
        EndpointPlaygroundCmd.class,
    }
)
public final class DbCmd extends DbListImpl {}
