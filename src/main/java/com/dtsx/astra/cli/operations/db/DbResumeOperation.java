package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.Optional;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.cli.operations.db.DbResumeOperation.DbResumeResult;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.HIBERNATED;

@RequiredArgsConstructor
public class DbResumeOperation implements Operation<DbResumeResult> {
    private final DbGateway dbGateway;
    private final DbResumeRequest request;

    public sealed interface DbResumeResult {}
    public record DatabaseAwaited(Duration waited) implements DbResumeResult {}
    public record DatabaseResumedAwaited(Duration waited) implements DbResumeResult {}
    public record DatabaseNeedsAwaiting(Database database) implements DbResumeResult {}
    public record DatabaseResumedNeedsAwaiting(Database database) implements DbResumeResult {}
    public record DatabaseAlreadyActiveNoWait() implements DbResumeResult {}

    public record DbResumeRequest(
        DbRef dbRef,
        LongRunningOptions lrOptions
    ) {}

    @Override
    public DbResumeResult execute() {
        val pair = dbGateway.resume(request.dbRef, Optional.of(request.lrOptions.timeout()).filter((_) -> !request.lrOptions.dontWait()));

        val initialStatus = pair.getLeft();
        val awaitedDuration = pair.getRight();

        if (initialStatus == ACTIVE) {
            return new DatabaseAlreadyActiveNoWait();
        }

        if (request.lrOptions.dontWait()) {
            val database = dbGateway.findOne(request.dbRef);

            return (initialStatus == HIBERNATED)
                ? new DatabaseResumedNeedsAwaiting(database)
                : new DatabaseNeedsAwaiting(database);
        }

        return (initialStatus == HIBERNATED)
            ? new DatabaseResumedAwaited(awaitedDuration)
            : new DatabaseAwaited(awaitedDuration);
    }
}
