package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;

@RequiredArgsConstructor
public class DbCreateOperation {
    private final DbGateway dbGateway;

    public record CreateDbRequest(
        String dbName,
        RegionName region,
        Optional<CloudProviderType> cloud,
        String db,
        String tier,
        Integer capacityUnits,
        boolean nonVector,
        boolean ifNotExists,
        LongRunningOptions lrOptions
    ) {}

    public sealed interface DbCreateResult {}
    public record DatabaseAlreadyExistsWithStatus(UUID dbId, DatabaseStatusType currStatus) implements DbCreateResult {}
    public record DatabaseAlreadyExistsAndIsNowActive(UUID dbId, DatabaseStatusType prevStatus, DbGateway.ResumeDbResult resumeResult) implements DbCreateResult {}
    public record DatabaseCreated(UUID dbId, Duration waitTime) implements DbCreateResult {}
    public record DatabaseCreationStarted(UUID dbId, DatabaseStatusType currStatus) implements DbCreateResult {}

    public DbCreateResult execute(CreateDbRequest request) {
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
            !request.nonVector
        );

        return switch (status) {
            case CreationStatus.AlreadyExists<Database>(var db) -> connectToExistingDb(request.dbName, UUID.fromString(db.getId()), db.getStatus(), request);
            case CreationStatus.Created<Database>(var db) -> connectToNewDb(UUID.fromString(db.getId()), db.getStatus(), request);
        };
    }

    private DbCreateResult connectToExistingDb(String dbName, UUID dbId, DatabaseStatusType dbStatus, CreateDbRequest request) {
        if (!request.ifNotExists) {
            throw new DbAlreadyExistsException(dbName, dbId);
        }

        if (request.lrOptions.dontWait()) {
            return new DatabaseAlreadyExistsWithStatus(dbId, dbStatus);
        }

        val resumeResult = dbGateway.resumeDb(DbRef.fromId(dbId), request.lrOptions.timeout());

        return new DatabaseAlreadyExistsAndIsNowActive(dbId, dbStatus, resumeResult);
    }

    private DbCreateResult connectToNewDb(UUID dbId, DatabaseStatusType dbStatus, CreateDbRequest request) {
        if (request.lrOptions.dontWait()) {
            return new DatabaseCreationStarted(dbId, dbStatus);
        }

        val awaitedDuration = dbGateway.waitUntilDbStatus(DbRef.fromId(dbId), ACTIVE, request.lrOptions.timeout());

        return new DatabaseCreated(dbId, awaitedDuration);
    }

    public static class DbAlreadyExistsException extends AstraCliException {
        public DbAlreadyExistsException(String dbName, UUID dbId) {
            super("""
              @|bold,red Error: Database '%s' already exists with ID '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see the existing dbs in your current org.
              - Pass the %s flag to skip this error if the db already exists.
            """.formatted(
                dbName,
                dbId,
                AstraColors.highlight("astra db list"),
                AstraColors.highlight("--if-not-exists")
            ));
        }
    }
}
