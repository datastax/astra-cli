package com.dtsx.astra.cli.domain.db;

import com.dtsx.astra.cli.completions.caches.DbCompletionsCache;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DbServiceCompletionsCacheWrapper implements DbService {
    private final DbService delegate;
    private final DbCompletionsCache cache;

    @Override
    public boolean waitUntilDbActive(String dbName, int timeout) {
        return delegate.waitUntilDbActive(dbName, timeout);
    }

    @Override
    public List<Database> findDatabases() {
        val databases = delegate.findDatabases();
        cache.update(databases.stream().map((db) -> db.getInfo().getName()).toList());
        return databases;
    }

    @Override
    public Database getDbInfo(String dbName) {
        return delegate.getDbInfo(dbName);
    }

    @Override
    public boolean resumeDb(String dbName) {
        return delegate.resumeDb(dbName);
    }

    @Override
    public CloudProviderType validateRegion(Optional<CloudProviderType> cloud, String region, boolean vectorOnly) {
        return delegate.validateRegion(cloud, region, vectorOnly);
    }

    @Override
    public String createDb(String dbName, String keyspace, String region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector) {
        return delegate.createDb(dbName, keyspace, region, cloud, tier, capacityUnits, vector);
    }
}
