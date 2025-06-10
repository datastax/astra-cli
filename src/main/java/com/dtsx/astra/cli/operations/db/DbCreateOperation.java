package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.exceptions.db.DbAlreadyExistsException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class DbCreateOperation {
    private final DbGateway dbGateway;

    public record CreateDbRequest(
        String dbName,
        String region,
        Optional<CloudProviderType> cloud,
        String keyspace,
        String tier,
        Integer capacityUnits,
        boolean nonVector,
        boolean ifNotExists,
        boolean dontWait,
        int timeout
    ) {}

    public sealed interface CreateDbResult {}

    public record DatabaseAlreadyExistsWithStatus(UUID dbId, DatabaseStatusType currStatus) implements CreateDbResult {}
    public record DatabaseAlreadyExistsAndIsNowActive(UUID dbId, DatabaseStatusType prevStatus, DbGateway.ResumeDbResult resumeResult) implements CreateDbResult {}
    public record DatabaseCreated(UUID dbId, Duration waitTime) implements CreateDbResult {}
    public record DatabaseCreationStarted(UUID dbId, DatabaseStatusType currStatus) implements CreateDbResult {}

    public CreateDbResult execute(CreateDbRequest request) {
        val dbRef = DbRef.fromNameUnsafe(request.dbName);
        val database = dbGateway.tryFindOneDb(dbRef);

        return database
            .map(value -> handleExistingDatabase(value, dbRef, request))
            .orElseGet(() -> handleCreateNewDatabase(dbRef, request));
    }

    private CreateDbResult handleExistingDatabase(Database db, DbRef dbRef, CreateDbRequest request) {
        val dbId = UUID.fromString(db.getId());

        if (!request.ifNotExists) {
            throw new DbAlreadyExistsException(dbRef, dbId);
        }

        if (request.dontWait) {
            return new DatabaseAlreadyExistsWithStatus(dbId, db.getStatus());
        }

        val originalStatus = db.getStatus();
        val resumeResult = dbGateway.resumeDb(dbRef, request.timeout);

        return new DatabaseAlreadyExistsAndIsNowActive(dbId, originalStatus, resumeResult);
    }

    private CreateDbResult handleCreateNewDatabase(DbRef dbRef, CreateDbRequest request) {
        val cloudProvider = dbGateway.findCloudForRegion(
            request.cloud,
            request.region,
            !request.nonVector
        );

        val createdId = dbGateway.createDb(
            request.dbName,
            request.keyspace,
            request.region,
            cloudProvider,
            request.tier,
            request.capacityUnits,
            !request.nonVector
        );

        val currentDb = dbGateway.findOneDb(DbRef.fromId(createdId));

        if (request.dontWait) {
            return new DatabaseCreationStarted(createdId, currentDb.getStatus());
        }

        val awaitedDuration = dbGateway.waitUntilDbActive(dbRef, request.timeout);

        return new DatabaseCreated(createdId, awaitedDuration);
    }
}
