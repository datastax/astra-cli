package com.dtsx.astra.cli.gateways;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.db.DbNotFoundException;
import com.dtsx.astra.cli.core.exceptions.internal.pcu.PcuGroupNotFoundException;
import com.dtsx.astra.cli.core.models.*;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.gateways.db.DbCache;
import com.dtsx.astra.cli.gateways.pcu.PcuCache;
import com.dtsx.astra.cli.gateways.pcu.vendored.PcuGroupOpsClient;
import com.dtsx.astra.cli.gateways.pcu.vendored.PcuGroupsClient;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.db.DbOpsClient;
import com.dtsx.astra.sdk.db.domain.DatabaseInfo;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.dtsx.astra.cli.core.output.ExitCode.UNIQUENESS_ISSUE;

@RequiredArgsConstructor
public class APIProviderImpl implements APIProvider {
    private final CliContext ctx;
    private final AstraToken token;
    private final AstraEnvironment env;
    private final DbCache dbCache;
    private final PcuCache pcuCache;

    @Override
    public AstraOpsClient astraOpsClient() {
        return new AstraOpsClient(token.unsafeUnwrap(), env);
    }

    @Override
    public PcuGroupsClient pcuGroupsClient() {
        return new PcuGroupsClient(token.unsafeUnwrap(), env);
    }

    @Override
    public DbOpsClient dbOpsClient(DbRef dbRef) {
        return astraOpsClient().db().database(resolveDbId(dbRef).toString());
    }

    @Override
    public PcuGroupOpsClient pcuGroupOpsClient(PcuRef pcuRef) {
        return pcuGroupsClient().group(resolvePcuId(pcuRef).toString());
    }

    @Override
    public Database dataApiDatabase(KeyspaceRef ksRef) {
        return dataApiClient().getDatabase(resolveDbId(ksRef.db()), resolveDbRegion(ksRef.db()), new DatabaseOptions().keyspace(ksRef.name()).token(token.unsafeUnwrap()));
    }

    @Override
    public DatabaseAdmin dataApiDatabaseAdmin(DbRef dbRef) {
        return dataApiClient().getAdmin().getDatabaseAdmin(resolveDbId(dbRef));
    }

    @Override
    public String restApiEndpoint(DbRef dbRef, AstraEnvironment env) {
        return ApiLocator.getApiRestEndpoint(env, resolveDbId(dbRef).toString(), resolveDbRegion(dbRef));
    }

    private DataAPIClient dataApiClient() {
        val destination = switch (env) {
            case PROD -> DataAPIDestination.ASTRA;
            case DEV -> DataAPIDestination.ASTRA_DEV;
            case TEST -> DataAPIDestination.ASTRA_TEST;
        };
        return new DataAPIClient(token.unsafeUnwrap(), new DataAPIClientOptions().destination(destination));
    }

    private UUID resolveDbId(DbRef ref) {
        val cachedId = dbCache.lookupDbId(ref);

        return cachedId.orElseGet(() -> ctx.log().loading("Resolving ID for database " + ctx.highlight(ref), (_) ->
            tryResolveDb(ref)
                .map(com.dtsx.astra.sdk.db.domain.Database::getId)
                .map(UUID::fromString)
                .orElseThrow(() -> new DbNotFoundException(ref))
        ));
    }

    private String resolveDbRegion(DbRef ref) {
        val cachedRegion = dbCache.lookupDbRegion(ref);

        return cachedRegion
            .map(RegionName::unwrap)
            .orElseGet(() -> ctx.log().loading("Resolving region for database " + ctx.highlight(ref), (_) ->
                tryResolveDb(ref)
                    .map(com.dtsx.astra.sdk.db.domain.Database::getInfo)
                    .map(DatabaseInfo::getRegion)
                    .orElseThrow(() -> new DbNotFoundException(ref))
            ));
    }

    private UUID resolvePcuId(PcuRef ref) {
        val cachedId = pcuCache.lookupPcuGroupId(ref);

        return cachedId.orElseGet(() -> ctx.log().loading("Resolving ID for PCU group " + ctx.highlight(ref), (_) ->
            tryResolvePcuGroup(ref)
                .map(PcuGroup::getId)
                .map(UUID::fromString)
                .orElseThrow(() -> new PcuGroupNotFoundException(ref))
        ));
    }

    @Override
    public Optional<com.dtsx.astra.sdk.db.domain.Database> tryResolveDb(@NotNull DbRef ref) {
        val cachedRef = dbCache.convertDbNameToIdIfCached(ref);

        val dbOpsClient = astraOpsClient().db();

        val dbInfo = cachedRef.<Optional<com.dtsx.astra.sdk.db.domain.Database>>fold(
            (id) -> dbOpsClient.findById(id.toString()),
            (name) -> {
                val all = dbOpsClient.findByName(name).toList();

                if (all.size() > 1) {
                    throw new AstraCliException(UNIQUENESS_ISSUE, """
                      @|bold,red Multiple databases with same name '%s' were found.|@
                    
                      Please use the target database's ID to resolve the conflict. Use @'!${cli.name} db list!@ to see each database's ID.
                    
                      Alternatively, if the command supports it, you can interactively select the target database by not passing a database identifier at all.
                    """.formatted(name), List.of(
                        new Hint("Example of using a database ID", "${cli.name} db get " + all.getFirst().getId()),
                        new Hint("Example of using interactive selection", "${cli.name} db get"),
                        new Hint("See all databases with their IDs", "${cli.name} db list")
                    ));
                }

                return (all.size() == 1) ? Optional.of(all.getFirst()) : Optional.empty();
            }
        );

        dbInfo.ifPresent(dbCache::cache);
        return dbInfo;
    }

    @Override
    public Optional<PcuGroup> tryResolvePcuGroup(@NotNull PcuRef ref) {
        val cachedRef = pcuCache.convertPcuTitleToIdIfCached(ref);

        val pcuGroupClient = pcuGroupsClient();

        val pcuGroup = cachedRef.<Optional<PcuGroup>>fold(
            (id) -> pcuGroupClient.findById(id.toString()),
            (name) -> {
                val all = pcuGroupClient.findByTitle(name).toList();

                if (all.size() > 1) {
                    throw new AstraCliException(UNIQUENESS_ISSUE, """
                      @|bold,red Multiple PCU groups with same name '%s' were found.|@
                    
                      Please use the target PCU group's ID to resolve the conflict. Use @'!${cli.name} pcu list!@ to see each PCU group's ID.
                    
                      Alternatively, if the command supports it, you can interactively select the target PCU group by not passing a PCU group identifier at all.
                    """.formatted(name), List.of(
                        new Hint("Example of using a PCU group ID", "${cli.name} pcu get " + all.getFirst().getId()),
                        new Hint("Example of using interactive selection", "${cli.name} pcu get"),
                        new Hint("See all PCU groups with their IDs", "${cli.name} pcu list")
                    ));
                }

                return (all.size() == 1) ? Optional.of(all.getFirst()) : Optional.empty();
            }
        );

        pcuGroup.ifPresent((pg) -> {
            val id = UUID.fromString(pg.getId());
            pcuCache.cachePcuGroupId(pg.getTitle(), id);
        });

        return pcuGroup;
    }
}
