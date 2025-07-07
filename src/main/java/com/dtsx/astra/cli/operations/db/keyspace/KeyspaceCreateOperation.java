package com.dtsx.astra.cli.operations.db.keyspace;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.cli.operations.db.keyspace.KeyspaceCreateOperation.*;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;

@RequiredArgsConstructor
public class KeyspaceCreateOperation implements Operation<KeyspaceCreateResult> {
    private final KeyspaceGateway keyspaceGateway;
    private final DbGateway dbGateway;
    private final KeyspaceCreateRequest request;

    public sealed interface KeyspaceCreateResult {}
    public record KeyspaceAlreadyExists() implements KeyspaceCreateResult {}
    public record KeyspaceCreated() implements KeyspaceCreateResult {}
    public record KeyspaceCreatedAndDbActive(Duration waitTime) implements KeyspaceCreateResult {}
    public record KeyspaceIllegallyAlreadyExists() implements KeyspaceCreateResult {}

    public record KeyspaceCreateRequest(
        KeyspaceRef keyspaceRef,
        boolean ifNotExists,
        LongRunningOptions lrOptions
    ) {}

    @Override
    public KeyspaceCreateResult execute() {
        val status = keyspaceGateway.create(request.keyspaceRef);

        return switch (status) {
            case CreationStatus.Created<?> _ -> handleKsCreated(request.keyspaceRef, request.lrOptions);
            case CreationStatus.AlreadyExists<?> _ -> handleKsAlreadyExists(request.ifNotExists);
        };
    }

    private KeyspaceCreateResult handleKsCreated(KeyspaceRef keyspaceRef, LongRunningOptions lrOptions) {
        if (lrOptions.dontWait()) {
            return new KeyspaceCreated();
        }

        val awaitedDuration = dbGateway.waitUntilDbStatus(keyspaceRef.db(), ACTIVE, lrOptions.timeout());
        return new KeyspaceCreatedAndDbActive(awaitedDuration);
    }

    private KeyspaceCreateResult handleKsAlreadyExists(boolean ifNotExists) {
        if (ifNotExists) {
            return new KeyspaceAlreadyExists();
        } else {
            return new KeyspaceIllegallyAlreadyExists();
        }
    }
}
