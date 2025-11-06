package com.dtsx.astra.cli.gateways.db;

import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.exceptions.internal.db.UnexpectedDbStatusException;
import com.dtsx.astra.cli.core.models.*;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.utils.HttpUtils;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseCreationRequest;
import com.dtsx.astra.sdk.db.domain.DatabaseFilter;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.graalvm.collections.Pair;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.awaitGenericStatus;
import static com.dtsx.astra.cli.core.output.ExitCode.IO_ISSUE;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.*;

@RequiredArgsConstructor
public class DbGatewayImpl implements DbGateway {
    private final CliContext ctx;
    private final APIProvider api;
    private final AstraToken token;
    private final AstraEnvironment env;
    private final DbCache dbCache;
    private final RegionGateway regionGateway;

    @Override
    public Stream<Database> findAll() {
        return ctx.log().loading("Fetching all databases", (_) -> 
            api.astraOpsClient().db().search(DatabaseFilter.builder()
                .limit(1000)
                .build())
        );
    }

    @Override
    public Optional<Database> tryFindOne(DbRef ref) {
        return ctx.log().loading("Fetching info for database " + ctx.highlight(ref), (_) -> (
            api.tryResolveDb(ref)
        ));
    }

    @Override
    public Optional<KeyspaceRef> tryFindDefaultKeyspace(DbRef dbRef) {
        if (dbCache.lookupDbDefaultKs(dbRef).isEmpty()) {
            findOne(dbRef); // populate cache
        }
        return dbCache.lookupDbDefaultKs(dbRef).map(ks -> KeyspaceRef.mkUnsafe(dbRef, ks));
    }

    @Override
    public boolean exists(DbRef ref) {
        return ctx.log().loading("Checking if database " + ctx.highlight(ref) + " exists", (_) -> tryFindOne(ref).isPresent());
    }

    @Override
    public Pair<DatabaseStatusType, Duration> resume(DbRef ref, Optional<Duration> timeout) {
        val currentStatus = ctx.log().loading("Fetching current status of db " + ctx.highlight(ref), (_) -> findOne(ref))
            .getStatus();

        val expectedStatuses = Arrays.stream(DatabaseStatusType.values())
            .filter(status -> status != ACTIVE && status != HIBERNATED && status != MAINTENANCE && status != INITIALIZING && status != PENDING && status != ASSOCIATING && status != RESUMING && status != UNPARKING)
            .toList();

        return switch (currentStatus) {
            case ACTIVE -> {
                yield Pair.create(currentStatus, Duration.ZERO);
            }
            case HIBERNATED -> {
                ctx.log().loading("Resuming database '%s'".formatted(ref), (_) -> {
                    resumeDbInternal(ref);
                    return null;
                });
                yield Pair.create(currentStatus, timeout.map((t) -> waitUntilDbStatus(ref, ACTIVE, t)).orElse(Duration.ZERO));
            }
            case MAINTENANCE, INITIALIZING, PENDING, ASSOCIATING, RESUMING, UNPARKING -> {
                yield Pair.create(currentStatus, timeout.map((t) -> waitUntilDbStatus(ref, ACTIVE, t)).orElse(Duration.ZERO));
            }
            default -> {
                throw new UnexpectedDbStatusException(ref, currentStatus, expectedStatuses);
            }
        };
    }

    private void resumeDbInternal(DbRef ref) {
        val endpoint = api.restApiEndpoint(ref, env) + "/v2/schemas/keyspace";

        val response = HttpUtils.GET(endpoint, c -> c, r -> r.header("X-Cassandra-Token", token.unsafeUnwrap()));

        if (response.statusCode() >= 400) {
            throw new AstraCliException(IO_ISSUE, """
              @|bold,red An error occurred while attempting to resume database %s|@
            
              The server returned the following response:
              %s
            """.formatted(ref, response.body()));
        }
    }

    @Override
    public Duration waitUntilDbStatus(DbRef ref, DatabaseStatusType target, Duration timeout) {
        return awaitGenericStatus(
            ctx,
            "database %s".formatted(ctx.highlight(ref)),
            target,
            () -> findOne(ref).getStatus(),
            ctx::highlight,
            timeout
        );
    }

    @Override
    public CloudProvider findCloudForRegion(Optional<CloudProvider> cloud, RegionName region, boolean vectorOnly) {
        val cloudRegions = regionGateway.findAllServerless(vectorOnly);

        if (cloud.isPresent()) {
            val cloudName = cloud.get().name().toLowerCase();

            if (!cloudRegions.containsKey(cloud.get())) {
                throw new OptionValidationException("cloud", "Cloud provider '%s' does not have any available%s regions".formatted(cloudName, (vectorOnly) ? " vector" : ""));
            }

            if (!cloudRegions.get(cloud.get()).containsKey(region.unwrap().toLowerCase())) {
                throw new OptionValidationException("region", "Region '%s' is not available for cloud provider '%s'".formatted(region.unwrap(), cloud.get()));
            }

            return cloud.get();
        }

        val matchingClouds = cloudRegions.entrySet().stream()
            .filter(entry -> entry.getValue().containsKey(region.unwrap().toLowerCase()))
            .map(Entry::getKey)
            .toList();

        return switch (matchingClouds.size()) {
            case 0 ->
                throw new OptionValidationException("region", "Region '%s' is not available for any cloud provider".formatted(region.unwrap()));
            case 1 ->
                matchingClouds.getFirst();
            default ->
                throw new OptionValidationException("region", "Region '%s' is available for multiple cloud providers: %s".formatted(
                    region.unwrap(), matchingClouds.stream().map(CloudProvider::name).toList()
                ));
        };
    }

    @Override
    public CreationStatus<Database> create(String name, String keyspace, RegionName region, CloudProvider cloud, String tier, int capacityUnits, boolean vector, boolean allowDuplicate) {
        if (!allowDuplicate) {
            val existingDb = ctx.log().loading("Checking if database " + ctx.highlight(name) + " already exists", (_) -> (
                tryFindOne(DbRef.fromNameUnsafe(name))
            ));

            if (existingDb.isPresent()) {
                return CreationStatus.alreadyExists(existingDb.get());
            }
        }

        val id = ctx.log().loading("Creating database %s".formatted(ctx.highlight(name)), (_) -> {
            val builder = DatabaseCreationRequest.builder()
                .name(name)
                .tier(tier)
                .capacityUnit(capacityUnits)
                .cloudProvider(cloud.toSdkType())
                .cloudRegion(region.unwrap())
                .keyspace(keyspace);

            if (vector) {
                builder.withVector();
            }

            return UUID.fromString(
                api.astraOpsClient().db().create(builder.build())
            );
        });

        dbCache.cacheDbId(name, id);
        dbCache.cacheDbRegion(id, region);
        dbCache.cacheDbDefaultKs(id, keyspace);

        val newDb = ctx.log().loading("Fetching info for newly created database " + ctx.highlight(name), (_) -> findOne(DbRef.fromId(id)));

        return CreationStatus.created(newDb);
    }

    @Override
    public DeletionStatus<DbRef> delete(DbRef ref) {
        if (!exists(ref)) {
            return DeletionStatus.notFound(ref);
        }

        ctx.log().loading("Deleting database " + ctx.highlight(ref), (_) -> {
            api.dbOpsClient(ref).delete();
            return null;
        });

        return DeletionStatus.deleted(ref);
    }

    @Override
    public FindEmbeddingProvidersResult findEmbeddingProviders(DbRef dbRef) {
        return ctx.log().loading("Fetching embedding providers for database " + ctx.highlight(dbRef), (_) -> {
            val admin = api.dataApiDatabaseAdmin(dbRef);
            return admin.findEmbeddingProviders();
        });
    }
}
