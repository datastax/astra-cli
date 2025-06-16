package com.dtsx.astra.cli.gateways.db;

import com.dtsx.astra.cli.core.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.GlobalInfoCache;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface DbGateway {
    static DbGateway mkDefault(String token, AstraEnvironment env, DbCompletionsCache dbCompletionsCache) {
        return new DbGatewayCompletionsCacheWrapper(new DbGatewayImpl(APIProvider.mkDefault(token, env), token, env, GlobalInfoCache.INSTANCE, OrgGateway.mkDefault(token, env)), dbCompletionsCache);
    }

    List<Database> findAllDbs();

    Database findOneDb(DbRef ref);

    Optional<Database> tryFindOneDb(DbRef ref);

    boolean dbExists(DbRef ref);

    List<String> downloadCloudSecureBundles(DbRef ref, String dbName, List<Datacenter> datacenters);

    record ResumeDbResult(boolean hadToBeResumed, Duration timeWaited) {
        public boolean wasAwaited() {
            return hadToBeResumed && !timeWaited.isZero();
        }
    }

    ResumeDbResult resumeDb(DbRef ref, int timeout);

    Duration waitUntilDbStatus(DbRef ref, DatabaseStatusType target, int timeout);

    CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, String region, boolean vectorOnly);

    CreationStatus<Database> createDb(String name, String keyspace, String region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector);

    DeletionStatus<DbRef> deleteDb(DbRef ref);
}
