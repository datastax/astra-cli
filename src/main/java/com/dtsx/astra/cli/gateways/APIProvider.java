package com.dtsx.astra.cli.gateways;

import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.databases.Database;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.pcu.vendored.PcuGroupOpsClient;
import com.dtsx.astra.cli.gateways.pcu.vendored.PcuGroupsClient;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.db.DbOpsClient;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface APIProvider {
    static APIProvider mkDefault(CliContext ctx, AstraToken token, AstraEnvironment env) {
        return new APIProviderImpl(ctx, token, env, GlobalInfoCache.INSTANCE, GlobalInfoCache.INSTANCE);
    }

    AstraOpsClient astraOpsClient();

    PcuGroupsClient pcuGroupsClient();

    DbOpsClient dbOpsClient(DbRef dbRef);

    PcuGroupOpsClient pcuGroupOpsClient(PcuRef pcuRef);

    Database dataApiDatabase(KeyspaceRef ksRef);

    DatabaseAdmin dataApiDatabaseAdmin(DbRef dbRef);

    String restApiEndpoint(DbRef dbRef, AstraEnvironment env);

    // I don't love having these here, but it's to avoid code duplication and circular dependencies
    //
    // I did try to use circular references via suppliers (w/ DbGateway & PcuGateway as members of ApiProvider),
    // but it got real messy.
    //
    // Trust me, it's best to leave it as is.
    //
    // It took me nearly an hour of scrapped work to learn this lesson, now you can learn it in a few seconds.
    //
    // You're welcome.
    Optional<com.dtsx.astra.sdk.db.domain.Database> tryResolveDb(@NotNull DbRef ref);
    Optional<PcuGroup> tryResolvePcuGroup(@NotNull PcuRef ref);
}
