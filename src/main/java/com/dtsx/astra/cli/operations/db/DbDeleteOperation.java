package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.TERMINATED;

@RequiredArgsConstructor
public class DbDeleteOperation {
    private final DbGateway dbGateway;

    public sealed interface DbDeleteResult {}
    public record DatabaseNotFound() implements DbDeleteResult {}
    public record DatabaseDeleted() implements DbDeleteResult {}
    public record DatabaseDeletedAndTerminated(Duration waitTime) implements DbDeleteResult {}

    public DbDeleteResult execute(DbRef dbRef, boolean ifExists, LongRunningOptions lrOptions) {
        val status = dbGateway.deleteDb(dbRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleDbDeleted(dbRef, lrOptions);
            case DeletionStatus.NotFound<?> _ -> handleDbNotFound(dbRef, ifExists);
        };
    }

    private DbDeleteResult handleDbDeleted(DbRef dbRef, LongRunningOptions lrOptions) {
        if (lrOptions.dontWait()) {
            return new DatabaseDeleted();
        }

        val awaitedDuration = dbGateway.waitUntilDbStatus(dbRef, TERMINATED, lrOptions.timeout());
        return new DatabaseDeletedAndTerminated(awaitedDuration);
    }

    private DbDeleteResult handleDbNotFound(DbRef dbRef, boolean ifExists) {
        if (ifExists) {
            return new DatabaseNotFound();
        } else {
            throw new DbNotFoundException(dbRef);
        }
    }

    public static class DbNotFoundException extends AstraCliException {
        public DbNotFoundException(DbRef dbRef) {
            super("""
              @|bold,red Error: Database '%s' could not be found.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see the existing dbs in your current org.
              - Pass the %s flag to skip this error if the db doesn't exist.
            """.formatted(
                dbRef,
                AstraColors.highlight("astra db list"),
                AstraColors.highlight("--if-exists")
            ));
        }
    }
}
