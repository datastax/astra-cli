package com.dtsx.astra.cli.domain.db;

import com.dtsx.astra.cli.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.domain.APIProvider;
import com.dtsx.astra.cli.domain.org.OrgService;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;
import java.util.Optional;

public interface DbService {
    static DbService mkDefault(String token, AstraEnvironment env, DbCompletionsCache dbCompletionsCache) {
        return new DbServiceCompletionsCacheWrapper(new DbServiceImpl(APIProvider.mkDefault(token, env), token, env, OrgService.mkDefault(token, env)), dbCompletionsCache);
    }

    boolean waitUntilDbActive(String dbName, int timeout);

    List<Database> findDatabases();

    Database getDbInfo(String dbName);

    boolean resumeDb(String dbName);

    CloudProviderType validateRegion(Optional<CloudProviderType> cloud, String region, boolean vectorOnly);

    String createDb(String dbName, String keyspace, String region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector);
}
