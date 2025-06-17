package com.dtsx.astra.cli.operations.db.keyspace;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;

@RequiredArgsConstructor
public class KeyspaceDeleteOperation {
    private final KeyspaceGateway keyspaceGateway;
    private final DbGateway dbGateway;

    public sealed interface KeyspaceDeleteResult {}
    public record KeyspaceNotFound() implements KeyspaceDeleteResult {}
    public record KeyspaceDeleted() implements KeyspaceDeleteResult {}
    public record KeyspaceDeletedAndDbActive(Duration waitTime) implements KeyspaceDeleteResult {}

    public KeyspaceDeleteResult execute(KeyspaceRef keyspaceRef, boolean ifExists, LongRunningOptions lrOptions) {
        val status = keyspaceGateway.deleteKeyspace(keyspaceRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleKsDeleted(keyspaceRef.db(), lrOptions);
            case DeletionStatus.NotFound<?> _ -> handleKsNotFound(keyspaceRef, ifExists);
        };
    }

    private KeyspaceDeleteResult handleKsDeleted(DbRef dbRef, LongRunningOptions lrOptions) {
        if (lrOptions.dontWait()) {
            return new KeyspaceDeleted();
        }

        val awaitedDuration = dbGateway.waitUntilDbStatus(dbRef, ACTIVE, lrOptions.timeout());
        return new KeyspaceDeletedAndDbActive(awaitedDuration);
    }

    private KeyspaceDeleteResult handleKsNotFound(KeyspaceRef keyspaceRef, boolean ifExists) {
        if (ifExists) {
            return new KeyspaceNotFound();
        } else {
            throw new KeyspaceNotFoundException(keyspaceRef);
        }
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
