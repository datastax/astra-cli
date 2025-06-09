package com.dtsx.astra.cli.domain.db;

import com.dtsx.astra.cli.domain.org.OrgService;
import com.dtsx.astra.cli.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.exceptions.db.DbNotFoundException;
import com.dtsx.astra.cli.exceptions.db.UnexpectedDbStatusException;
import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.cli.utils.EnumFolder;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.domain.RegionType;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.dtsx.astra.cli.output.AstraColors.highlight;
import static com.dtsx.astra.cli.output.AstraColors.highlightStatus;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.*;

@RequiredArgsConstructor
public class DbServiceImpl implements DbService {
    private final DbDao dbDao;
    private final OrgService orgService;

    @Override
    public List<Database> findDatabases() {
        return AstraLogger.loading("Fetching all databases", (_) -> dbDao.findAll());
    }

    @Override
    public Database getDbInfo(DbRef ref) {
        return AstraLogger.loading("Fetching info for database " + highlight(ref), (_) -> dbDao.findOne(ref));
    }

    @Override
    public Optional<Database> tryGetDbInfo(DbRef ref) {
        try {
            return Optional.of(getDbInfo(ref));
        } catch (DbNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean dbExists(DbRef ref) {
        try {
            dbDao.findOne(ref);
            return true;
        } catch (DbNotFoundException e) {
            return false;
        }
    }

    @Override
    public ResumeDbResult resumeDb(DbRef ref, int timeout) {
        val currentStatus = AstraLogger.loading("Fetching current status of db " + highlight(ref), (_) -> getDbInfo(ref))
            .getStatus();

        return new EnumFolder<DatabaseStatusType, ResumeDbResult>(DatabaseStatusType.class)
            .on(ACTIVE, () -> {
                AstraLogger.debug("Database '%s' is already active".formatted(ref));
                return new ResumeDbResult(false, Duration.ZERO);
            })
            .on(HIBERNATED, () -> {
                AstraLogger.loading("Resuming database '%s'".formatted(ref), (_) -> {
                    dbDao.resume(ref);
                    return null;
                });

                return new ResumeDbResult(true, waitUntilDbActive(ref, timeout));
            })
            .on(MAINTENANCE, INITIALIZING, PENDING, () -> {
                AstraLogger.debug("Database '%s' is of status '%s', and will be available soon".formatted(ref, currentStatus));
                return new ResumeDbResult(false, Duration.ZERO);
            })
            .exhaustive((expected, _) -> {
                throw new UnexpectedDbStatusException(ref, currentStatus, expected);
            })
            .run(currentStatus);
    }

    @Override
    public CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, String region, boolean vectorOnly) {
        val regionType = (vectorOnly) ? RegionType.VECTOR : RegionType.ALL;
        val cloudRegions = orgService.getDbServerlessRegions(regionType);

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
        return AstraLogger.loading("Creating database %s".formatted(highlight(name)), (_) -> (
            dbDao.create(name, keyspace, region, cloud, tier, capacityUnits, vector)
        ));
    }

    @Override
    public Duration waitUntilDbActive(DbRef ref, int timeout) {
        val timeoutDuration = Duration.ofSeconds(timeout);
        val startTime = System.currentTimeMillis();

        var status = new AtomicReference<>(
            AstraLogger.loading("Fetching initial status of database %s".formatted(highlight(ref)), (_) -> lookupDbStatus(ref))
        );

        if (status.get().equals(ACTIVE)) {
            return Duration.ZERO;
        }

        val initialMessage = "Waiting for database %s to become active (currently %s)"
            .formatted(highlight(ref), highlightStatus(status.get()));

        return AstraLogger.loading(initialMessage, (updateMsg) -> {
            while (!status.get().equals(ACTIVE)) {
                val elapsed = Duration.ofMillis(System.currentTimeMillis() - startTime);
                
                if (timeout > 0 && elapsed.compareTo(timeoutDuration) >= 0) {
                    break;
                }

                try {
                    updateMsg.accept(
                        "Waiting for database %s to become active (currently %s, elapsed: %ds)"
                            .formatted(highlight(ref), highlightStatus(status.get()), elapsed.toSeconds())
                    );

                    Thread.sleep(5000);

                    status.set(lookupDbStatus(ref));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            return Duration.ofMillis(System.currentTimeMillis() - startTime);
        });
    }

    private DatabaseStatusType lookupDbStatus(DbRef ref) {
        return dbDao.findOne(ref).getStatus();
    }
}
