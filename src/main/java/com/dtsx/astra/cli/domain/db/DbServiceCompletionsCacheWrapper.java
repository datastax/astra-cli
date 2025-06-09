package com.dtsx.astra.cli.domain.db;

import com.dtsx.astra.cli.completions.caches.DbCompletionsCache;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.graalvm.collections.Pair;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class DbServiceCompletionsCacheWrapper implements DbService {
    private final DbService delegate;
    private final DbCompletionsCache cache;

    @Override
    public Duration waitUntilDbActive(DbRef ref, int timeout) {
        return delegate.waitUntilDbActive(ref, timeout);
    }

    @Override
    public List<Database> findDatabases() {
        val databases = delegate.findDatabases();
        cache.update(databases.stream().map((db) -> db.getInfo().getName()).toList());
        return databases;
    }

    @Override
    public Database getDbInfo(DbRef ref) {
        return delegate.getDbInfo(ref);
    }

    @Override
    public Optional<Database> tryGetDbInfo(DbRef ref) {
        return delegate.tryGetDbInfo(ref);
    }

    @Override
    public boolean dbExists(DbRef ref) {
        return delegate.dbExists(ref);
    }

    @Override
    public ResumeDbResult resumeDb(DbRef ref, int timeout) {
        return delegate.resumeDb(ref, timeout);
    }

    @Override
    public CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, String region, boolean vectorOnly) {
        return delegate.findCloudForRegion(cloud, region, vectorOnly);
    }

    @Override
    public UUID createDb(String name, String keyspace, String region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector) {
        return delegate.createDb(name, keyspace, region, cloud, tier, capacityUnits, vector);
    }
}
