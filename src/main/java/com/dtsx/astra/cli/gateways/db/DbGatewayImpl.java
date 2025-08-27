package com.dtsx.astra.cli.gateways.db;

import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.exceptions.internal.db.DbNotFoundException;
import com.dtsx.astra.cli.core.exceptions.internal.db.UnexpectedDbStatusException;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.sdk.db.domain.*;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.graalvm.collections.Pair;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.*;

@RequiredArgsConstructor
public class DbGatewayImpl implements DbGateway {
    private final APIProvider api;
    private final AstraToken token;
    private final AstraEnvironment env;
    private final DbCache dbCache;
    private final RegionGateway regionGateway;

    @Override
    public Stream<Database> findAll() {
        return AstraLogger.loading("Fetching all databases", (_) -> 
            api.astraOpsClient().db().search(DatabaseFilter.builder()
                .limit(1000)
                .build())
        );
    }

    @Override
    public Database findOne(DbRef ref) {
        return AstraLogger.loading("Fetching info for database " + highlight(ref), (_) ->
            api.dbOpsClient(ref).find().orElseThrow(() -> new DbNotFoundException(ref))
        );
    }

    @Override
    public Optional<Database> tryFindOne(DbRef ref) {
        try {
            return Optional.of(findOne(ref));
        } catch (DbNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean exists(DbRef ref) {
        return AstraLogger.loading("Checking if database " + highlight(ref) + " exists", (_) -> tryFindOne(ref).isPresent());
    }

    @Override
    public Pair<DatabaseStatusType, Duration> resume(DbRef ref, Optional<Integer> timeout) {
        val currentStatus = AstraLogger.loading("Fetching current currStatus of db " + highlight(ref), (_) -> findOne(ref))
            .getStatus();

        val expectedStatuses = Arrays.stream(DatabaseStatusType.values())
            .filter(status -> status != ACTIVE && status != HIBERNATED && status != MAINTENANCE && status != INITIALIZING && status != PENDING)
            .toList();

        return switch (currentStatus) {
            case ACTIVE -> {
                yield Pair.create(currentStatus, Duration.ZERO);
            }
            case HIBERNATED -> {
                AstraLogger.loading("Resuming database '%s'".formatted(ref), (_) -> {
                    resumeDbInternal(ref);
                    return null;
                });
                yield Pair.create(currentStatus, timeout.map((t) -> waitUntilDbStatus(ref, ACTIVE, t)).orElse(Duration.ZERO));
            }
            case MAINTENANCE, INITIALIZING, PENDING -> {
                yield Pair.create(currentStatus, timeout.map((t) -> waitUntilDbStatus(ref, ACTIVE, t)).orElse(Duration.ZERO));
            }
            default -> {
                throw new UnexpectedDbStatusException(ref, currentStatus, expectedStatuses);
            }
        };
    }

    @SneakyThrows
    private void resumeDbInternal(DbRef ref) {
        try {
            val endpoint = api.restApiEndpoint(ref, env) + "/v2/schemas/keyspace";

            @Cleanup val client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

            val request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("X-Cassandra-Token", token.unwrap())
                .GET()
                .build();

            val response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 500) {
                throw new AstraCliException("""
                  @|bold,red An error occurred while attempting to resume database %s|@
                
                  The server returned the following response:
                  %s
                """.formatted(ref, response.body()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Duration waitUntilDbStatus(DbRef ref, DatabaseStatusType target, int timeout) {
        val timeoutDuration = Duration.ofSeconds(timeout);
        val startTime = System.currentTimeMillis();

        var status = new AtomicReference<>(
            AstraLogger.loading("Fetching initial status of database %s".formatted(highlight(ref)), (_) -> findOne(ref).getStatus())
        );

        if (status.get().equals(target)) {
            return Duration.ZERO;
        }

        val initialMessage = "Waiting for database %s to become %s (currently %s)"
            .formatted(highlight(ref), highlight(target), highlight(status.get()));

        return AstraLogger.loading(initialMessage, (updateMsg) -> {
            var cycles = 0;

            while (!status.get().equals(target)) {
                val elapsed = Duration.ofMillis(System.currentTimeMillis() - startTime);
                
                if (timeout > 0 && elapsed.compareTo(timeoutDuration) >= 0) {
                    break;
                }

                try {
                    updateMsg.accept(
                        "Waiting for database %s to become %s (currently %s, elapsed: %ds)"
                            .formatted(highlight(ref), highlight(target), AstraColors.highlight(status.get()), elapsed.toSeconds())
                    );

                    if (cycles % 5 == 0) {
                        updateMsg.accept(
                            "Checking if database %s is status %s (currently %s, elapsed: %ds)"
                                .formatted(highlight(ref), highlight(target), AstraColors.highlight(status.get()), elapsed.toSeconds())
                        );

                        status.set(findOne(ref).getStatus());
                    }

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                cycles++;
            }

            return Duration.ofMillis(System.currentTimeMillis() - startTime);
        });
    }

    @Override
    public CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, RegionName region, boolean vectorOnly) {
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
                    region.unwrap(), matchingClouds.stream().map(CloudProviderType::name).toList()
                ));
        };
    }

    @Override
    public CreationStatus<Database> create(String name, String keyspace, RegionName region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector, boolean allowDuplicate) {
        if (!allowDuplicate) {
            val existingDb = AstraLogger.loading("Checking if database " + highlight(name) + " already exists", (_) -> (
                tryFindOne(DbRef.fromNameUnsafe(name))
            ));

            if (existingDb.isPresent()) {
                return CreationStatus.alreadyExists(existingDb.get());
            }
        }

        val id = AstraLogger.loading("Creating database %s".formatted(highlight(name)), (_) -> {
            val builder = DatabaseCreationRequest.builder()
                .name(name)
                .tier(tier)
                .capacityUnit(capacityUnits)
                .cloudProvider(cloud)
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

        val newDb = AstraLogger.loading("Fetching info for newly created database " + highlight(name), (_) -> findOne(DbRef.fromId(id)));

        return CreationStatus.created(newDb);
    }

    @Override
    public DeletionStatus<DbRef> delete(DbRef ref) {
        if (!exists(ref)) {
            return DeletionStatus.notFound(ref);
        }

        AstraLogger.loading("Deleting database " + highlight(ref), (_) -> {
            api.dbOpsClient(ref).delete();
            return null;
        });

        return DeletionStatus.deleted(ref);
    }

    @Override
    public FindEmbeddingProvidersResult findEmbeddingProviders(DbRef dbRef) {
        return AstraLogger.loading("Fetching embedding providers for database " + highlight(dbRef), (_) -> {
            val admin = api.dataApiDatabaseAdmin(dbRef);
            return admin.findEmbeddingProviders();
        });
    }
}
