package com.dtsx.astra.cli.gateways.db;

import com.dtsx.astra.cli.config.AstraHome;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.core.exceptions.db.CouldNotResumeDbException;
import com.dtsx.astra.cli.core.exceptions.db.DbNotFoundException;
import com.dtsx.astra.cli.core.exceptions.db.UnexpectedDbStatusException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.utils.EnumFolder;
import com.dtsx.astra.sdk.db.domain.*;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.Utils;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.*;

@RequiredArgsConstructor
public class DbGatewayImpl implements DbGateway {
    private final APIProvider api;
    private final String token;
    private final AstraEnvironment env;
    private final DbCache dbCache;
    private final OrgGateway orgGateway;

    @Override
    public List<Database> findAllDbs() {
        return AstraLogger.loading("Fetching all databases", (_) -> 
            api.astraOpsClient().db().search(DatabaseFilter.builder()
                .limit(1000)
                .build()).toList()
        );
    }

    @Override
    public Database findOneDb(DbRef ref) {
        return AstraLogger.loading("Fetching info for database " + highlight(ref), (_) ->
            api.dbOpsClient(ref).find().orElseThrow(() -> new DbNotFoundException(ref))
        );
    }

    @Override
    public Optional<Database> tryFindOneDb(DbRef ref) {
        try {
            return Optional.of(findOneDb(ref));
        } catch (DbNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean dbExists(DbRef ref) {
        return tryFindOneDb(ref).isPresent();
    }

    @Override
    public ResumeDbResult resumeDb(DbRef ref, int timeout) {
        val currentStatus = AstraLogger.loading("Fetching current currStatus of db " + highlight(ref), (_) -> findOneDb(ref))
            .getStatus();

        return new EnumFolder<DatabaseStatusType, ResumeDbResult>(DatabaseStatusType.class)
            .on(ACTIVE, () -> {
                AstraLogger.debug("Database '%s' is already active".formatted(ref));
                return new ResumeDbResult(false, Duration.ZERO);
            })
            .on(HIBERNATED, () -> {
                AstraLogger.loading("Resuming database '%s'".formatted(ref), (_) -> {
                    resumeDbInternal(ref);
                    return null;
                });

                return new ResumeDbResult(true, waitUntilDbActive(ref, timeout));
            })
            .on(MAINTENANCE, INITIALIZING, PENDING, () -> {
                AstraLogger.debug("Database '%s' is of currStatus '%s', and will be available soon".formatted(ref, currentStatus));
                return new ResumeDbResult(false, Duration.ZERO);
            })
            .exhaustive((expected, _) -> {
                throw new UnexpectedDbStatusException(ref, currentStatus, expected);
            })
            .run(currentStatus);
    }

    @Override
    public List<String> downloadCloudSecureBundles(DbRef ref, String dbName, List<Datacenter> datacenters) {
        val dbOpsClient = api.dbOpsClient(ref);

        return datacenters.stream()
            .map((datacenter) -> (
                AstraLogger.loading("Downloading secure connect bundle for database %s in region %s".formatted(highlight(ref), highlight(datacenter.getRegion())), (_) -> {
                    val scbName = dbOpsClient.buildScbFileName(dbName, datacenter.getRegion());
                    val scbPath = new File(AstraHome.Dirs.useScb(), scbName);

                    if (!scbPath.exists()) {
                        Utils.downloadFile(datacenter.getSecureBundleUrl(), scbPath.getAbsolutePath());
                    }

                    return scbPath.getAbsolutePath();
                })
            ))
            .toList();
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
                .header("X-Cassandra-Token", token)
                .GET()
                .build();

            val response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 500) {
                throw new CouldNotResumeDbException(ref, response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Duration waitUntilDbActive(DbRef ref, int timeout) {
        val timeoutDuration = Duration.ofSeconds(timeout);
        val startTime = System.currentTimeMillis();

        var status = new AtomicReference<>(
            AstraLogger.loading("Fetching initial currStatus of database %s".formatted(highlight(ref)), (_) -> findOneDb(ref).getStatus())
        );

        if (status.get().equals(ACTIVE)) {
            return Duration.ZERO;
        }

        val initialMessage = "Waiting for database %s to become active (currently %s)"
            .formatted(highlight(ref), AstraColors.highlight(status.get()));

        return AstraLogger.loading(initialMessage, (updateMsg) -> {
            while (!status.get().equals(ACTIVE)) {
                val elapsed = Duration.ofMillis(System.currentTimeMillis() - startTime);
                
                if (timeout > 0 && elapsed.compareTo(timeoutDuration) >= 0) {
                    break;
                }

                try {
                    updateMsg.accept(
                        "Waiting for database %s to become active (currently %s, elapsed: %ds)"
                            .formatted(highlight(ref), AstraColors.highlight(status.get()), elapsed.toSeconds())
                    );

                    Thread.sleep(5000);

                    status.set(findOneDb(ref).getStatus());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            return Duration.ofMillis(System.currentTimeMillis() - startTime);
        });
    }

    @Override
    public CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, String region, boolean vectorOnly) {
        val regionType = (vectorOnly) ? RegionType.VECTOR : RegionType.ALL;
        val cloudRegions = orgGateway.getDbServerlessRegions(regionType);

        if (cloud.isPresent()) {
            val cloudName = cloud.get().name();

            if (!cloudRegions.containsKey(cloudName.toLowerCase())) {
                throw new OptionValidationException("cloud", "Cloud provider '%s' does not have any available%s regions".formatted(cloudName, (vectorOnly) ? " vector" : ""));
            }

            if (!cloudRegions.get(cloudName.toLowerCase()).containsKey(region.toLowerCase())) {
                throw new OptionValidationException("region", "Region '%s' is not available for cloud provider '%s'".formatted(region, cloudName));
            }

            return cloud.get();
        }

        val matchingClouds = cloudRegions.entrySet().stream()
            .filter(entry -> entry.getValue().containsKey(region.toLowerCase()))
            .map(entry -> CloudProviderType.valueOf(entry.getKey().toUpperCase()))
            .toList();

        return switch (matchingClouds.size()) {
            case 0 ->
                throw new OptionValidationException("region", "Region '%s' is not available for any cloud provider".formatted(region));
            case 1 ->
                matchingClouds.getFirst();
            default ->
                throw new OptionValidationException("region", "Region '%s' is available for multiple cloud providers: %s".formatted(
                    region, matchingClouds.stream().map(CloudProviderType::name).toList()
                ));
        };
    }

    @Override
    public UUID createDb(String name, String keyspace, String region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector) {
        return AstraLogger.loading("Creating database %s".formatted(highlight(name)), (_) -> {
            val builder = DatabaseCreationRequest.builder()
                .name(name)
                .tier(tier)
                .capacityUnit(capacityUnits)
                .cloudProvider(cloud)
                .cloudRegion(region)
                .keyspace(keyspace);

            if (vector) {
                builder.withVector();
            }

            val id = UUID.fromString(
                api.astraOpsClient().db().create(builder.build())
            );

            dbCache.cacheDbId(name, id);
            dbCache.cacheDbRegion(id, region);
            return id;
        });
    }
}
