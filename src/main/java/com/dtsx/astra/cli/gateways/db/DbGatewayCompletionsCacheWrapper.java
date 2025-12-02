package com.dtsx.astra.cli.gateways.db;

import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.utils.MiscUtils.toFn;

@RequiredArgsConstructor
public class DbGatewayCompletionsCacheWrapper implements DbGateway {
    private final DbGateway delegate;
    private final CompletionsCache cache;

    @Override
    public Stream<Database> findAll() {
        val databases = delegate.findAll().toList();
        cache.setCache(databases.stream().map(i -> i.getInfo().getName()).toList());
        return databases.stream();
    }

    @Override
    public Optional<Database> tryFindOne(DbRef ref) {
        val res = delegate.tryFindOne(ref);

        if (res.isPresent()) {
            cache.addToCache(res.get().getInfo().getName());
        } else {
            removeRefFromCache(ref);
        }

        return res;
    }

    @Override
    public Optional<KeyspaceRef> tryFindDefaultKeyspace(DbRef dbRef) {
        val res = delegate.tryFindDefaultKeyspace(dbRef);
        res.ifPresent((_) -> addRefToCache(dbRef));
        return res;
    }

    @Override
    public boolean exists(DbRef ref) {
        val exists = delegate.exists(ref);

        if (exists) {
            addRefToCache(ref);
        } else {
            removeRefFromCache(ref);
        }

        return exists;
    }

    @Override
    public Pair<DatabaseStatusType, Duration> resume(DbRef ref, Optional<Duration> timeout) {
        val res = delegate.resume(ref, timeout);
        addRefToCache(ref);
        return res;
    }

    @Override
    public Duration waitUntilDbStatus(DbRef ref, DatabaseStatusType target, Duration timeout) {
        val duration = delegate.waitUntilDbStatus(ref, target, timeout);
        addRefToCache(ref);
        return duration;
    }

    @Override
    public CloudProvider findCloudForRegion(Optional<CloudProvider> cloud, RegionName region, boolean vectorOnly) {
        return delegate.findCloudForRegion(cloud, region, vectorOnly);
    }

    @Override
    public CreationStatus<Database> create(String name, String keyspace, RegionName region, CloudProvider cloud, String tier, int capacityUnits, boolean vector, boolean allowDuplicate) {
        val status = delegate.create(name, keyspace, region, cloud, tier, capacityUnits, vector, allowDuplicate);
        cache.addToCache(name);
        return status;
    }

    @Override
    public DeletionStatus<DbRef> delete(DbRef ref) {
        val status = delegate.delete(ref);
        removeRefFromCache(ref);
        return status;
    }

    @Override
    public FindEmbeddingProvidersResult findEmbeddingProviders(DbRef dbRef) {
        return delegate.findEmbeddingProviders(dbRef);
    }

    private void addRefToCache(DbRef ref) {
        ref.fold(
            _ -> null,
            toFn(cache::addToCache)
        );
    }

    private void removeRefFromCache(DbRef ref) {
        ref.fold(
            _ -> null,
            toFn(cache::removeFromCache)
        );
    }
}
