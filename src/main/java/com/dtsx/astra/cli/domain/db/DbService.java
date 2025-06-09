package com.dtsx.astra.cli.domain.db;

import com.dtsx.astra.cli.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.domain.org.OrgService;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DbService {
    static DbService mkDefault(String token, AstraEnvironment env, DbCompletionsCache dbCompletionsCache) {
        return new DbServiceCompletionsCacheWrapper(new DbServiceImpl(DbDao.mkDefault(token, env), OrgService.mkDefault(token, env)), dbCompletionsCache);
    }

    Duration waitUntilDbActive(DbRef ref, int timeout);

    List<Database> findDatabases();

    Database getDbInfo(DbRef ref);

    Optional<Database> tryGetDbInfo(DbRef ref);

    boolean dbExists(DbRef ref);

    record ResumeDbResult(boolean hadToBeResumed, Duration timeWaited) {
        public boolean wasAwaited() {
            return hadToBeResumed && !timeWaited.isZero();
        }
    }

    ResumeDbResult resumeDb(DbRef ref, int timeout);

    CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, String region, boolean vectorOnly);

    UUID createDb(String name, String keyspace, String region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector);
}
