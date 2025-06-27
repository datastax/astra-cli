package com.dtsx.astra.cli.gateways.db;

import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.dtsx.astra.cli.core.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.models.Token;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.GlobalInfoCache;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.graalvm.collections.Pair;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface DbGateway {
    static DbGateway mkDefault(Token token, AstraEnvironment env, DbCompletionsCache dbCompletionsCache) {
        return new DbGatewayCompletionsCacheWrapper(new DbGatewayImpl(APIProvider.mkDefault(token, env), token, env, GlobalInfoCache.INSTANCE, RegionGateway.mkDefault(token, env)), dbCompletionsCache);
    }

    List<Database> findAllDbs();

    Database findOneDb(DbRef ref);

    Optional<Database> tryFindOneDb(DbRef ref);

    boolean dbExists(DbRef ref);

    List<String> downloadCloudSecureBundles(DbRef ref, String dbName, List<Datacenter> datacenters);

    Pair<DatabaseStatusType, Duration> resumeDb(DbRef ref, Optional<Integer> timeout);

    Duration waitUntilDbStatus(DbRef ref, DatabaseStatusType target, int timeout);

    CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, RegionName region, boolean vectorOnly);

    CreationStatus<Database> createDb(String name, String keyspace, RegionName region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector);

    DeletionStatus<DbRef> deleteDb(DbRef ref);

    FindEmbeddingProvidersResult findEmbeddingProviders(DbRef dbRef);
}
