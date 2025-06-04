package com.dtsx.astra.cli.domain;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.dtsx.astra.cli.exceptions.db.DatabaseNameNotUniqueException;
import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.cli.utils.StringUtils;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.graalvm.collections.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface APIProvider {
    static APIProvider mkDefault(String token, AstraEnvironment env) {
        return new Default(token, env);
    }

    AstraOpsClient devopsApiClient();

    Optional<com.datastax.astra.client.databases.Database> dataApiDatabase(String dbName, @Nullable String region, String keyspace);

    com.datastax.astra.client.admin.AstraDBAdmin dataApiAstraAdmin(String dbName);

    @RequiredArgsConstructor
    class Default implements APIProvider {
        private final String token;
        private final AstraEnvironment env;

        private @Nullable AstraOpsClient cachedDevopsApiClient;
        private @Nullable DataAPIClient cachedDataApiClient;

        @Override
        public AstraOpsClient devopsApiClient() {
            if (cachedDevopsApiClient == null) {
                cachedDevopsApiClient = new AstraOpsClient(token, env);
            }
            return cachedDevopsApiClient;
        }

        @Override
        public Optional<com.datastax.astra.client.databases.Database> dataApiDatabase(String dbName, @Nullable String region, String keyspace) {
            if (StringUtils.isUUID(dbName) && region != null) {
                return Optional.of(
                    dataApiClient().getDatabase(UUID.fromString(dbName), region, new DatabaseOptions().keyspace(keyspace).token(token))
                );
            }

            val dbIdRegion = getDbIdRegion(dbName);

            if (dbIdRegion.isPresent()) {
                val dbId = dbIdRegion.get().getLeft();
                val dbRegion = dbIdRegion.get().getRight();

                return Optional.of(
                    dataApiClient().getDatabase(dbId, dbRegion, new DatabaseOptions().keyspace(keyspace).token(token))
                );
            }

            return Optional.empty();
        }

        @Override
        public com.datastax.astra.client.admin.AstraDBAdmin dataApiAstraAdmin(String dbName) {
            return dataApiClient().getAdmin();
        }

        private DataAPIClient dataApiClient() {
            if (cachedDataApiClient == null) {
                val destination = switch (env) {
                    case PROD -> DataAPIDestination.ASTRA;
                    case DEV -> DataAPIDestination.ASTRA_DEV;
                    case TEST -> DataAPIDestination.ASTRA_TEST;
                };

                cachedDataApiClient = new DataAPIClient(token, new DataAPIClientOptions().destination(destination));
            }
            return cachedDataApiClient;
        }

        private Optional<Pair<UUID, String>> getDbIdRegion(String dbName) throws DatabaseNameNotUniqueException {
            return AstraLogger.loading("Fetching database id+default region for '%s'".formatted(dbName), (_) -> {
                val dbsClient = devopsApiClient().db();

                List<Database> dbs;

                try {
                    dbs = dbsClient.findById(UUID.fromString(dbName).toString()).stream().toList();
                } catch (IllegalArgumentException _) {
                    dbs = dbsClient.findByName(dbName).toList();
                }

                if (dbs.size() > 1) {
                    throw new DatabaseNameNotUniqueException(dbName);
                }

                if (1 == dbs.size()) {
                    return Optional.of(Pair.create(UUID.fromString(dbs.getFirst().getId()), dbs.getFirst().getInfo().getRegion()));
                }

                return Optional.empty();
            });
        }
    }
}
