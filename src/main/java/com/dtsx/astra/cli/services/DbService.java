package com.dtsx.astra.cli.services;

import com.dtsx.astra.cli.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.services.impls.DbServiceImpl;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;

public interface DbService {
    static DbService mkDefault(String token, AstraEnvironment env, DbCompletionsCache dbCompletionsCache) {
        return new DbServiceImpl(token, env, dbCompletionsCache);
    }

    void waitUntilDbActive(String dbName, int timeout);

    List<Database> findDatabases();
}
