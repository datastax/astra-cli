package com.dtsx.astra.cli.gateways;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.dtsx.astra.cli.core.models.Token;
import com.dtsx.astra.cli.gateways.db.DbCache;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.exceptions.db.DbNameNotUniqueException;
import com.dtsx.astra.cli.core.exceptions.db.DbNotFoundException;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.db.DbOpsClient;
import com.dtsx.astra.sdk.db.domain.DatabaseInfo;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class APIProviderImpl implements APIProvider {
    private final Token token;
    private final AstraEnvironment env;
    private final DbCache dbCache;

    @Override
    public AstraOpsClient astraOpsClient() {
        return new AstraOpsClient(token.unwrap(), env);
    }

    @Override
    public DbOpsClient dbOpsClient(DbRef dbRef) {
        return astraOpsClient().db().database(resolveId(dbRef).toString());
    }

    @Override
    public Database dataApiDatabase(KeyspaceRef ksRef) {
        return dataApiClient().getDatabase(resolveId(ksRef.db()), resolveRegion(ksRef.db()), new DatabaseOptions().keyspace(ksRef.name()).token(token.unwrap()));
    }

    @Override
    public DatabaseAdmin dataApiDatabaseAdmin(DbRef dbRef) {
        return dataApiClient().getAdmin().getDatabaseAdmin(resolveId(dbRef));
    }

    @Override
    public String restApiEndpoint(DbRef dbRef, AstraEnvironment env) {
        return ApiLocator.getApiRestEndpoint(env, resolveId(dbRef).toString(), resolveRegion(dbRef));
    }

    private DataAPIClient dataApiClient() {
        val destination = switch (env) {
            case PROD -> DataAPIDestination.ASTRA;
            case DEV -> DataAPIDestination.ASTRA_DEV;
            case TEST -> DataAPIDestination.ASTRA_TEST;
        };
        return new DataAPIClient(token.unwrap(), new DataAPIClientOptions().destination(destination));
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

        return cachedRegion
            .map(RegionName::unwrap)
            .orElseGet(() -> AstraLogger.loading("Resolving region for database " + highlight(ref), (_) ->
                tryResolveDb(ref)
                    .map(com.dtsx.astra.sdk.db.domain.Database::getInfo)
                    .map(DatabaseInfo::getRegion)
                    .orElseThrow(() -> new DbNotFoundException(ref))
            ));
    }

    public Optional<com.dtsx.astra.sdk.db.domain.Database> tryResolveDb(@NotNull DbRef ref) {
        val cachedRef = dbCache.convertDbNameToIdIfCached(ref);

        val dbOpsClient = new AstraOpsClient(token.unwrap(), env).db();

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
            dbCache.cacheDbRegion(id, RegionName.mkUnsafe(info.getInfo().getRegion()));
        });

        return dbInfo;
    }
}
