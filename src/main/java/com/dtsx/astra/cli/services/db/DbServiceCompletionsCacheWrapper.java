package com.dtsx.astra.cli.services.db;

import com.dtsx.astra.cli.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.services.APIProvider;
import com.dtsx.astra.cli.services.DbService;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseFilter;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

@RequiredArgsConstructor
public class DbServiceCompletionsCacheWrapper implements DbService {
    private final DbService delegate;
    private final DbCompletionsCache cache;

    @Override
    public void waitUntilDbActive(String dbName, int timeout) {
        delegate.waitUntilDbActive(dbName, timeout);
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
}
