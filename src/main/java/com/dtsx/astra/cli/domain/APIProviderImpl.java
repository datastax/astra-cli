package com.dtsx.astra.cli.domain;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.dtsx.astra.cli.domain.db.DbCache;
import com.dtsx.astra.cli.domain.db.DbRef;
import com.dtsx.astra.cli.domain.db.keyspaces.KeyspaceRef;
import com.dtsx.astra.cli.exceptions.db.DbNameNotUniqueException;
import com.dtsx.astra.cli.exceptions.db.DbNotFoundException;
import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.db.AstraDBOpsClient;
import com.dtsx.astra.sdk.db.domain.DatabaseInfo;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

import static com.dtsx.astra.cli.output.AstraColors.highlight;

@RequiredArgsConstructor
public class APIProviderImpl implements APIProvider {
    private final String token;
    private final AstraEnvironment env;
    private final DbCache dbCache;

    @Override
    public AstraOpsClient astraOpsClient() {
        return new AstraOpsClient(token, env);
    }

    @Override
    public AstraDBOpsClient dbOpsClient() {
        return astraOpsClient().db();
    }

    @Override
    public Database dataApiDatabase(KeyspaceRef ksRef) {
        return dataApiClient().getDatabase(resolveId(ksRef.db()), resolveRegion(ksRef.db()), new DatabaseOptions().keyspace(ksRef.name()).token(token));
    }

    @Override
    public String restApiEndpoint(DbRef dbRef) {
        return "";
    }

    private DataAPIClient dataApiClient() {
        val destination = switch (env) {
            case PROD -> DataAPIDestination.ASTRA;
            case DEV -> DataAPIDestination.ASTRA_DEV;
            case TEST -> DataAPIDestination.ASTRA_TEST;
        };
        return new DataAPIClient(token, new DataAPIClientOptions().destination(destination));
    }

    private UUID resolveId(DbRef ref) {
        val cachedId = dbCache.lookupDbId(ref);

        return cachedId.orElseGet(() -> AstraLogger.loading("Resolving ID for database " + highlight(ref), (_) ->
            tryResolveDb(ref)
                .map(com.dtsx.astra.sdk.db.domain.Database::getId)
                .map(UUID::fromString)
                .orElseThrow(() -> new DbNotFoundException(ref))
        ));
    }

    private String resolveRegion(DbRef ref) {
        val cachedRegion = dbCache.lookupDbRegion(ref);

        return cachedRegion.orElseGet(() -> AstraLogger.loading("Resolving region for database " + highlight(ref), (_) ->
            tryResolveDb(ref)
                .map(com.dtsx.astra.sdk.db.domain.Database::getInfo)
                .map(DatabaseInfo::getRegion)
                .orElseThrow(() -> new DbNotFoundException(ref))
        ));
    }

    public Optional<com.dtsx.astra.sdk.db.domain.Database> tryResolveDb(@NotNull DbRef ref) {
        val cachedRef = dbCache.convertDbNameToIdIfCached(ref);

        val dbOpsClient = new AstraOpsClient(token, env).db();

        val dbInfo = cachedRef.<Optional<com.dtsx.astra.sdk.db.domain.Database>>fold(
            (id) -> dbOpsClient.findById(id.toString()),
            (name) -> {
                val all = dbOpsClient.findByName(name).toList();

                if (all.size() > 1) {
                    throw new DbNameNotUniqueException(name);
                }

                return (all.size() == 1) ? Optional.of(all.getFirst()) : Optional.empty();
            }
        );

        dbInfo.ifPresent((info) -> {
            val id = UUID.fromString(info.getId());
            dbCache.cacheDbId(info.getInfo().getName(), id);
            dbCache.cacheDbRegion(id, info.getInfo().getRegion());
        });

        return dbInfo;
    }
}
