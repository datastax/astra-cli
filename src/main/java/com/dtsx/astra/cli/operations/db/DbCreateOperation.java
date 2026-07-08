package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.models.RegionRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.operations.Operation;
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
    private final RegionGateway regionGateway;
    private final PcuGateway pcuGateway;
    private final CreateDbRequest request;

    public enum ExistingBehavior {
        FAIL,
        CREATE_IF_NOT_EXISTS,
        ALLOW_DUPLICATES
    }

    public record CreateDbRequest(
        String dbName,
        RegionRef region,
        Optional<CloudProvider> cloud,
        String db,
        String tier,
        Integer capacityUnits,
        boolean nonVector,
        Optional<PcuRef> pcuGroup,
        ExistingBehavior existingBehavior,
        LongRunningOptions lrOptions,
        boolean skipRegionVerification
    ) {}

    public sealed interface DbCreateResult {}
    public record DatabaseAlreadyExistsWithStatus(UUID dbId, DatabaseStatusType currStatus) implements DbCreateResult {}
    public record DatabaseAlreadyExistsIllegallyWithStatus(UUID dbId, DatabaseStatusType currStatus) implements DbCreateResult {}
    public record DatabaseAlreadyExistsAndIsActive(UUID dbId, DatabaseStatusType prevStatus, Duration awaited) implements DbCreateResult {}
    public record DatabaseCreated(UUID dbId, Duration waitTime) implements DbCreateResult {}
    public record DatabaseCreationStarted(UUID dbId, DatabaseStatusType currStatus) implements DbCreateResult {}

    @Override
    public DbCreateResult execute() {
        val cloudProvider = (request.skipRegionVerification && request.cloud.isPresent())
            ? request.cloud.get()
            : regionGateway.findCloudForRegion(
                request.cloud,
                request.region,
                !request.nonVector
            );

        val pcuId = request.pcuGroup.map((ref) -> ref.fold(
            id -> id,
            _ -> pcuGateway.findOne(ref).getId()
        ));

        val status = dbGateway.create(
            request.dbName,
            request.db,
            request.region,
            cloudProvider,
            request.tier,
            request.capacityUnits,
            !request.nonVector,
            pcuId,
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

        val awaited = dbGateway.resume(DbRef.fromId(dbId), Optional.of(request.lrOptions.timeout()));

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
