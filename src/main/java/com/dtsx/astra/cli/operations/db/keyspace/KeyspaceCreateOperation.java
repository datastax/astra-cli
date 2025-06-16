package com.dtsx.astra.cli.operations.db.keyspace;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;

import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;

@RequiredArgsConstructor
public class KeyspaceCreateOperation {
    private final KeyspaceGateway keyspaceGateway;
    private final DbGateway dbGateway;

    public sealed interface KeyspaceCreateResult {}
    public record KeyspaceAlreadyExists() implements KeyspaceCreateResult {}
    public record KeyspaceCreated() implements KeyspaceCreateResult {}
    public record KeyspaceCreatedAndDbActive(Duration waitTime) implements KeyspaceCreateResult {}

    public KeyspaceCreateResult execute(KeyspaceRef keyspaceRef, boolean ifNotExists, boolean dontWait, int timeout) {
        val status = keyspaceGateway.createKeyspace(keyspaceRef);

        return switch (status) {
            case CreationStatus.Created<?> _ -> handleKsCreated(keyspaceRef, dontWait, timeout);
            case CreationStatus.AlreadyExists<?> _ -> handleKsAlreadyExists(keyspaceRef, ifNotExists);
        };
    }

    private KeyspaceCreateResult handleKsCreated(KeyspaceRef keyspaceRef, boolean dontWait, int timeout) {
        if (dontWait) {
            return new KeyspaceCreated();
        }

        val awaitedDuration = dbGateway.waitUntilDbStatus(keyspaceRef.db(), ACTIVE, timeout);
        return new KeyspaceCreatedAndDbActive(awaitedDuration);
    }

    private KeyspaceCreateResult handleKsAlreadyExists(KeyspaceRef keyspaceRef, boolean ifNotExists) {
        if (ifNotExists) {
            return new KeyspaceAlreadyExists();
        } else {
            throw new KeyspaceAlreadyExistsException(keyspaceRef);
        }
    }

    public static class KeyspaceAlreadyExistsException extends AstraCliException {
        public KeyspaceAlreadyExistsException(KeyspaceRef keyspaceRef) {
            super("""
              @|bold,red Error: Keyspace '%s' already exists in database '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see the existing keyspaces.
              - Pass the %s flag to skip this error if the keyspace already exists.
            """.formatted(
                keyspaceRef,
                keyspaceRef.db(),
                AstraColors.highlight("astra db list-keyspaces " + keyspaceRef.db()),
                AstraColors.highlight("--if-not-exists")
            ));
        }
    }
}
