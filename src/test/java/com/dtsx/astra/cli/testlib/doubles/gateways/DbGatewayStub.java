package com.dtsx.astra.cli.testlib.doubles.gateways;

import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import org.graalvm.collections.Pair;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.testlib.doubles.Utils.methodIllegallyCalled;

public class DbGatewayStub extends GatewayStub implements DbGateway {
    @Override
    public Stream<Database> findAll() {
        return methodIllegallyCalled();
    }

    @Override
    public Optional<Database> tryFindOne(DbRef ref) {
        return methodIllegallyCalled();
    }

    @Override
    public boolean exists(DbRef ref) {
        return methodIllegallyCalled();
    }

    @Override
    public Pair<DatabaseStatusType, Duration> resume(DbRef ref, Optional<Integer> timeout) {
        return methodIllegallyCalled();
    }

    @Override
    public Duration waitUntilDbStatus(DbRef ref, DatabaseStatusType target, int timeout) {
        return methodIllegallyCalled();
    }

    @Override
    public CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, RegionName region, boolean vectorOnly) {
        return methodIllegallyCalled();
    }

    @Override
    public CreationStatus<Database> create(String name, String keyspace, RegionName region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector, boolean allowDuplicate) {
        return methodIllegallyCalled();
    }

    @Override
    public DeletionStatus<DbRef> delete(DbRef ref) {
        return methodIllegallyCalled();
    }

    @Override
    public FindEmbeddingProvidersResult findEmbeddingProviders(DbRef dbRef) {
        return methodIllegallyCalled();
    }
}
