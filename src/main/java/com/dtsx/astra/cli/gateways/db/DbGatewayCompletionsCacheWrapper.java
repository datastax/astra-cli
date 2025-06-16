package com.dtsx.astra.cli.gateways.db;

import com.dtsx.astra.cli.core.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.utils.MiscUtils.*;

@RequiredArgsConstructor
public class DbGatewayCompletionsCacheWrapper implements DbGateway {
    private final DbGateway delegate;
    private final DbCompletionsCache cache;

    @Override
    public List<Database> findAllDbs() {
        val databases = delegate.findAllDbs();
        setCache(databases.stream().map(i -> i.getInfo().getName()).toList());
        return databases;
    }

    @Override
    public Database findOneDb(DbRef ref) {
        val db = delegate.findOneDb(ref);
        addToCache(db.getInfo().getName());
        return db;
    }

    @Override
    public Optional<Database> tryFindOneDb(DbRef ref) {
        val res = delegate.tryFindOneDb(ref);

        if (res.isPresent()) {
            addToCache(res.get().getInfo().getName());
        } else {
            removeFromCache(ref);
        }

        return res;
    }

    @Override
    public boolean dbExists(DbRef ref) {
        val exists = delegate.dbExists(ref);

        if (exists) {
            addToCache(ref);
        } else {
            removeFromCache(ref);
        }

        return exists;
    }

    @Override
    public ResumeDbResult resumeDb(DbRef ref, int timeout) {
        val res = delegate.resumeDb(ref, timeout);
        addToCache(ref);
        return res;
    }

    @Override
    public Duration waitUntilDbStatus(DbRef ref, DatabaseStatusType target, int timeout) {
        val duration = delegate.waitUntilDbStatus(ref, target, timeout);
        addToCache(ref);
        return duration;
    }

    @Override
    public List<String> downloadCloudSecureBundles(DbRef ref, String dbName, List<Datacenter> datacenters) {
        val downloadPaths = delegate.downloadCloudSecureBundles(ref, dbName, datacenters);
        addToCache(ref);
        return downloadPaths;
    }

    @Override
    public CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, String region, boolean vectorOnly) {
        return delegate.findCloudForRegion(cloud, region, vectorOnly);
    }

    @Override
    public CreationStatus<Database> createDb(String name, String keyspace, String region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector) {
        val status = delegate.createDb(name, keyspace, region, cloud, tier, capacityUnits, vector);
        addToCache(name);
        return status;
    }

    @Override
    public DeletionStatus<DbRef> deleteDb(DbRef ref) {
        val status = delegate.deleteDb(ref);
        removeFromCache(ref);
        return status;
    }

    private void setCache(List<String> dbNames) {
        cache.update((_) -> new HashSet<>(dbNames));
    }

    private void addToCache(String dbName) {
        cache.update((s) -> setAdd(s, dbName));
    }

    private void addToCache(DbRef ref) {
        ref.fold(
            _ -> null,
            name -> toVoid(() -> cache.update((s) -> setAdd(s, name)))
        );
    }

    private void removeFromCache(DbRef ref) {
        ref.fold(
            _ -> null,
            name -> toVoid(() -> cache.update((s) -> setDel(s, name)))
        );
    }
}
