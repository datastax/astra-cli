package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.cli.operations.db.DbCreateOperation.DbCreateResult;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;

@RequiredArgsConstructor
public class DbCreateOperation implements Operation<DbCreateResult> {
    private final DbGateway dbGateway;
    private final CreateDbRequest request;

    public enum ExistingBehavior {
        FAIL,
        CREATE_IF_NOT_EXISTS,
        ALLOW_DUPLICATES
    }

    public record CreateDbRequest(
        String dbName,
        RegionName region,
        Optional<CloudProviderType> cloud,
        String db,
        String tier,
        Integer capacityUnits,
        boolean nonVector,
        ExistingBehavior existingBehavior,
        LongRunningOptions lrOptions
    ) {}

    public sealed interface DbCreateResult {}
    public record DatabaseAlreadyExistsWithStatus(UUID dbId, DatabaseStatusType currStatus) implements DbCreateResult {}
    public record DatabaseAlreadyExistsIllegallyWithStatus(UUID dbId, DatabaseStatusType currStatus) implements DbCreateResult {}
    public record DatabaseAlreadyExistsAndIsActive(UUID dbId, DatabaseStatusType prevStatus, Duration awaited) implements DbCreateResult {}
    public record DatabaseCreated(UUID dbId, Duration waitTime) implements DbCreateResult {}
    public record DatabaseCreationStarted(UUID dbId, DatabaseStatusType currStatus) implements DbCreateResult {}

    @Override
    public DbCreateResult execute() {
        val cloudProvider = dbGateway.findCloudForRegion(
            request.cloud,
            request.region,
            !request.nonVector
        );

        val status = dbGateway.createDb(
            request.dbName,
            request.db,
            request.region,
            cloudProvider,
            request.tier,
            request.capacityUnits,
            !request.nonVector,
            request.existingBehavior == ExistingBehavior.ALLOW_DUPLICATES
        );

        val db = status.value();
        val dbId = UUID.fromString(db.getId());

        if (status instanceof CreationStatus.Created<?>) {
            return connectToNewDb(dbId, db.getStatus(), request);
        }

        if (request.existingBehavior == ExistingBehavior.FAIL) {
            return new DatabaseAlreadyExistsIllegallyWithStatus(dbId, db.getStatus());
        }

        return connectToExistingDb(dbId, db.getStatus(), request);
    }

    private DbCreateResult connectToExistingDb(UUID dbId, DatabaseStatusType dbStatus, DbCreateOperation.CreateDbRequest request) {
        if (request.lrOptions.dontWait()) {
            return new DbCreateOperation.DatabaseAlreadyExistsWithStatus(dbId, dbStatus);
        }

        val awaited = dbGateway.resumeDb(DbRef.fromId(dbId), Optional.of(request.lrOptions.timeout()));

        return new DbCreateOperation.DatabaseAlreadyExistsAndIsActive(dbId, dbStatus, awaited.getRight());
    }

    private DbCreateResult connectToNewDb(UUID dbId, DatabaseStatusType dbStatus, CreateDbRequest request) {
        if (request.lrOptions.dontWait()) {
            return new DbCreateOperation.DatabaseCreationStarted(dbId, dbStatus);
        }

        val awaitedDuration = dbGateway.waitUntilDbStatus(DbRef.fromId(dbId), ACTIVE, request.lrOptions.timeout());

        return new DbCreateOperation.DatabaseCreated(dbId, awaitedDuration);
    }
}
