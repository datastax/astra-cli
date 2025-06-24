package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.commands.db.cdc.CdcCreateCmd;
import com.dtsx.astra.cli.commands.db.cdc.CdcDeleteCmd;
import com.dtsx.astra.cli.commands.db.cdc.CdcListCmd;
import com.dtsx.astra.cli.commands.db.collections.CollectionCreateCmd;
import com.dtsx.astra.cli.commands.db.collections.CollectionDeleteCmd;
import com.dtsx.astra.cli.commands.db.collections.CollectionListCmd;
import com.dtsx.astra.cli.commands.db.collections.CollectionTruncateCmd;
import com.dtsx.astra.cli.commands.db.misc.EmbeddingProvidersListCmd;
import com.dtsx.astra.cli.commands.db.table.TableDeleteCmd;
import com.dtsx.astra.cli.commands.db.table.TableListCmd;
import com.dtsx.astra.cli.commands.db.table.TableTruncateCmd;
import com.dtsx.astra.cli.commands.db.endpoints.EndpointApiCmd;
import com.dtsx.astra.cli.commands.db.endpoints.EndpointPlaygroundCmd;
import com.dtsx.astra.cli.commands.db.endpoints.EndpointSwaggerCmd;
import com.dtsx.astra.cli.commands.db.keyspace.KeyspaceCreateCmd;
import com.dtsx.astra.cli.commands.db.keyspace.KeyspaceDeleteCmd;
import com.dtsx.astra.cli.commands.db.keyspace.KeyspaceListCmd;
import com.dtsx.astra.cli.commands.db.misc.CloudsListCmd;
import com.dtsx.astra.cli.commands.db.region.*;
import picocli.CommandLine.Command;

@Command(
    name = "db",
    subcommands = {
        DbListCmd.class,
        DbGetCmd.class,
        DbCreateCmd.class,
        DbDeleteCmd.class,
        DbCreateDotEnv.class,
        DbDownloadScbCmd.class,
        EmbeddingProvidersListCmd.class,
        DbResumeCmd.class,
        KeyspaceListCmd.class,
        KeyspaceCreateCmd.class,
        KeyspaceDeleteCmd.class,
        CollectionListCmd.class,
        CollectionCreateCmd.class,
        CollectionDeleteCmd.class,
        CollectionTruncateCmd.class,
        TableListCmd.class,
        TableDeleteCmd.class,
        TableTruncateCmd.class,
        CdcListCmd.class,
        CdcCreateCmd.class,
        CdcDeleteCmd.class,
        RegionCreateCmd.class,
        RegionDeleteCmd.class,
        RegionListCmd.class,
        RegionListClassicCmd.class,
        RegionListServerlessCmd.class,
        RegionListVectorCmd.class,
        CloudsListCmd.class,
        EndpointSwaggerCmd.class,
        EndpointApiCmd.class,
        EndpointPlaygroundCmd.class,
    }
)
public final class DbCmd extends DbListImpl {}
