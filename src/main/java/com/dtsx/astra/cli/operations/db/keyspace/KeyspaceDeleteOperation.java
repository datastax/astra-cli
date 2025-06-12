package com.dtsx.astra.cli.operations.db.keyspace;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway.InternalKeyspaceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;

@RequiredArgsConstructor
public class KeyspaceDeleteOperation {
    private final KeyspaceGateway keyspaceGateway;
    private final DbGateway dbGateway;

    public sealed interface KeyspaceDeleteResult {}
    public record KeyspaceNotFound() implements KeyspaceDeleteResult {}
    public record KeyspaceDeleted() implements KeyspaceDeleteResult {}
    public record KeyspaceDeletedAndDbActive(Duration waitTime) implements KeyspaceDeleteResult {}

    public KeyspaceDeleteResult execute(KeyspaceRef keyspaceRef, boolean ifExists, boolean dontWait, int timeout) {
        try {
            keyspaceGateway.deleteKeyspace(keyspaceRef);
        } catch (InternalKeyspaceNotFoundException e) {
            if (ifExists) {
                return new KeyspaceNotFound();
            } else {
                throw new KeyspaceNotFoundException(e.keyspaceRef);
            }
        }

        if (dontWait) {
            return new KeyspaceDeleted();
        }

        val awaitedDuration = dbGateway.waitUntilDbActive(keyspaceRef.db(), timeout);
        return new KeyspaceDeletedAndDbActive(awaitedDuration);
    }

    public static class KeyspaceNotFoundException extends AstraCliException {
        public KeyspaceNotFoundException(KeyspaceRef keyspaceRef) {
            super("""
              @|bold,red Error: Keyspace '%s' does not exist in database '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see the existing keyspaces.
              - Pass the %s flag to skip this error if the keyspace doesn't exist.
            """.formatted(
                keyspaceRef,
                keyspaceRef.db(),
                AstraColors.highlight("astra db list-keyspaces " + keyspaceRef.db()),
                AstraColors.highlight("--if-exists")
            ));
        }
    }
}
