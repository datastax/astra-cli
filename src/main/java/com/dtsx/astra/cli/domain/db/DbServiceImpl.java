package com.dtsx.astra.cli.domain.db;

import com.datastax.astra.client.admin.AstraDBAdmin;
import com.dtsx.astra.cli.exceptions.db.OptionValidationException;
import com.dtsx.astra.cli.exceptions.db.UnexpectedDatabaseStatusException;
import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.cli.domain.APIProvider;
import com.dtsx.astra.cli.domain.org.OrgService;
import com.dtsx.astra.cli.utils.EnumFolder;
import com.dtsx.astra.cli.utils.StringUtils;
import com.dtsx.astra.sdk.db.domain.*;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.*;

public class DbServiceImpl implements DbService {
    private final APIProvider apiProvider;
    private final DatabaseDao databaseDao;
    private final OrgService orgService;

    public DbServiceImpl(APIProvider apiProvider, String token, AstraEnvironment env, OrgService orgService) {
        this.apiProvider = apiProvider;
        this.databaseDao = new DatabaseDao(apiProvider, token, env);
        this.orgService = orgService;
    }

    @Override
    public boolean waitUntilDbActive(String dbName, int timeout) {
        val astraAdmin = apiProvider.dataApiAstraAdmin(dbName);
        val db = apiProvider.dataApiDatabase(dbName, null, null).orElseThrow(() -> new DatabaseNotFoundException(dbName));

        val dbId = (!StringUtils.isUUID(dbName))
            ? apiProvider.dataApiDatabase(dbName, null, null).orElseThrow(() -> new DatabaseNotFoundException(dbName)).getId()
            : db.getId();

        return retryUntilTimeoutOrSuccess(astraAdmin, dbName, dbId, timeout);
    }

    @Override
    public List<Database> findDatabases() {
        return AstraLogger.loading("Fetching databases", (_) -> (
            apiProvider.devopsApiClient().db()
                .search(DatabaseFilter.builder().limit(1000).build())
                .toList()
        ));
    }

    @Override
    public Database getDbInfo(String dbName) {
        return databaseDao.getDatabase(dbName);
    }

    @Override
    public boolean resumeDb(String dbName) {
        val currentStatus = AstraLogger.loading("Fetching current status of db '%s'".formatted(dbName), (_) -> getDbInfo(dbName))
            .getStatus();

        return new EnumFolder<DatabaseStatusType, Boolean>(DatabaseStatusType.class)
            .on(ACTIVE, () -> {
                AstraLogger.debug("Database '%s' is already active".formatted(dbName));
                return false;
            })
            .on(HIBERNATED, () -> AstraLogger.loading("Resuming database '%s'".formatted(dbName), (_) -> {
                try {
                    apiProvider.dataApiDatabase(dbName, null, null).orElseThrow(() -> new DatabaseNotFoundException(dbName)).listCollections();
                } catch (IllegalStateException _) {}
                return true;
            }))
            .on(MAINTENANCE, INITIALIZING, PENDING, () -> {
                AstraLogger.debug("Database '%s' is of status '%s', and will be available soon".formatted(dbName, currentStatus));
                return false;
            })
            .exhaustive((expected, _) -> {
                throw new UnexpectedDatabaseStatusException(dbName, currentStatus, expected);
            })
            .run(currentStatus);
    }

    @Override
    public CloudProviderType validateRegion(Optional<CloudProviderType> cloud, String region, boolean vectorOnly) {
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
    public String createDb(String dbName, String keyspace, String region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector) {
        val builder = DatabaseCreationRequest.builder()
            .name(dbName)
            .tier(tier)
            .capacityUnit(capacityUnits)
            .cloudProvider(cloud)
            .cloudRegion(region)
            .keyspace(keyspace);

        if (vector) {
            builder.withVector();
        }

        return AstraLogger.loading("Creating database '%s'".formatted(dbName), (_) -> (
            apiProvider.devopsApiClient().db().create(builder.build())
        ));
    }

    private boolean retryUntilTimeoutOrSuccess(AstraDBAdmin astraAdmin, String dbName, UUID dbId, int timeout) {
        final int[] retries = {0};

        var status = new AtomicReference<>(
            astraAdmin.getDatabaseInfo(dbId).getRawDevopsResponse().getStatus()
        );

        val initialMessage = "Waiting for database '%s' to become active (currently %s)"
            .formatted(AstraColors.BLUE_300.use(dbName), AstraColors.colorStatus(status.get()));

        return AstraLogger.loading(initialMessage, (updateMsg) -> {
            while (((timeout == 0) || (retries[0]++ < timeout)) && !status.get().equals(ACTIVE)) {
                try {
                    updateMsg.accept(
                        "Waiting for database '%s' to become active (currently %s, attempt #%d)".formatted(AstraColors.BLUE_300.use(dbName), AstraColors.colorStatus(status.get()), retries[0] + 1)
                    );

                    Thread.sleep(1000);

                    AstraLogger.loading("Checking if database '%s' is active (currently %s, attempt #%d)".formatted(AstraColors.BLUE_300.use(dbName), AstraColors.colorStatus(status.get()), retries[0] + 1), (_) -> {
                        status.set(astraAdmin.getDatabaseInfo(dbId).getRawDevopsResponse().getStatus());
                        return null;
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            return retries[0] > 1;
        });
    }
}
