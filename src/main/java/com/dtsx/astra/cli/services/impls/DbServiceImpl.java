package com.dtsx.astra.cli.services.impls;

import com.dtsx.astra.cli.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.services.DbService;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseFilter;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import java.util.List;

@RequiredArgsConstructor
public class DbServiceImpl implements DbService {
    private final String token;
    private final AstraEnvironment env;
    private final DbCompletionsCache dbCompletionsCache;

    private @Nullable AstraOpsClient cachedDevopsApiClient;

    private AstraOpsClient devopsApiClient() {
        if (cachedDevopsApiClient == null) {
            cachedDevopsApiClient = new AstraOpsClient(token, env);
        }
        return cachedDevopsApiClient;
    }

    @Override
    public void waitUntilDbActive(String dbName, int timeout) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Database> findDatabases() {
        val databases = devopsApiClient().db()
            .search(DatabaseFilter.builder().limit(1000).build())
            .toList();

        dbCompletionsCache.update(databases.stream().map((db) -> db.getInfo().getName()).toList());
        return databases;
    }
}
