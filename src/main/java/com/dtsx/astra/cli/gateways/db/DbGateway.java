package com.dtsx.astra.cli.gateways.db;

import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.internal.db.DbNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import org.graalvm.collections.Pair;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

public interface DbGateway extends SomeGateway {
    Stream<Database> findAll();

    Optional<Database> tryFindOne(DbRef ref);

    default Database findOne(DbRef ref) {
        return tryFindOne(ref).orElseThrow(() -> new DbNotFoundException(ref));
    }

    boolean exists(DbRef ref);

    Pair<DatabaseStatusType, Duration> resume(DbRef ref, Optional<Integer> timeout);

    Duration waitUntilDbStatus(DbRef ref, DatabaseStatusType target, int timeout);

    CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, RegionName region, boolean vectorOnly);

    CreationStatus<Database> create(String name, String keyspace, RegionName region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector, boolean allowDuplicate);

    DeletionStatus<DbRef> delete(DbRef ref);

    FindEmbeddingProvidersResult findEmbeddingProviders(DbRef dbRef);
}
