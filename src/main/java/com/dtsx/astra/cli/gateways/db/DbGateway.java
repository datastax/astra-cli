package com.dtsx.astra.cli.gateways.db;

import com.dtsx.astra.cli.core.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.GlobalInfoCache;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DbGateway {
    static DbGateway mkDefault(String token, AstraEnvironment env, DbCompletionsCache dbCompletionsCache) {
        return new DbGatewayCompletionsCacheWrapper(new DbGatewayImpl(APIProvider.mkDefault(token, env), token, env, GlobalInfoCache.INSTANCE, OrgGateway.mkDefault(token, env)), dbCompletionsCache);
    }

    List<Database> findAllDbs();

    Database findOneDb(DbRef ref);

    Optional<Database> tryFindOneDb(DbRef ref);

    boolean dbExists(DbRef ref);

    record ResumeDbResult(boolean hadToBeResumed, Duration timeWaited) {
        public boolean wasAwaited() {
            return hadToBeResumed && !timeWaited.isZero();
        }
    }

    ResumeDbResult resumeDb(DbRef ref, int timeout);

    Duration waitUntilDbActive(DbRef ref, int timeout);

    CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, String region, boolean vectorOnly);

    UUID createDb(String name, String keyspace, String region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector);
}
