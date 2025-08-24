package com.dtsx.astra.cli.gateways.db;

import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.GlobalInfoCache;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.graalvm.collections.Pair;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

public interface DbGateway {
    static DbGateway mkDefault(AstraToken token, AstraEnvironment env, CompletionsCache dbCompletionsCache) {
        return new DbGatewayCompletionsCacheWrapper(new DbGatewayImpl(APIProvider.mkDefault(token, env), token, env, GlobalInfoCache.INSTANCE, RegionGateway.mkDefault(token, env)), dbCompletionsCache);
    }

    Database findOne(DbRef ref);

    Stream<Database> findAll();

    Optional<Database> tryFindOne(DbRef ref);

    boolean exists(DbRef ref);

    Pair<DatabaseStatusType, Duration> resume(DbRef ref, Optional<Integer> timeout);

    Duration waitUntilDbStatus(DbRef ref, DatabaseStatusType target, int timeout);

    CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, RegionName region, boolean vectorOnly);

    CreationStatus<Database> create(String name, String keyspace, RegionName region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector, boolean allowDuplicate);

    DeletionStatus<DbRef> delete(DbRef ref);

    FindEmbeddingProvidersResult findEmbeddingProviders(DbRef dbRef);
}
