package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.cli.operations.db.DbDeleteOperation.DbDeleteResult;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.TERMINATED;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.TERMINATING;

@RequiredArgsConstructor
public class DbDeleteOperation implements Operation<DbDeleteResult> {
    private final DbGateway dbGateway;
    private final DbDeleteRequest request;

    public sealed interface DbDeleteResult {}
    public record DatabaseNotFound() implements DbDeleteResult {}
    public record DatabaseDeleted() implements DbDeleteResult {}
    public record DatabaseAlreadyDeleting() implements DbDeleteResult {}
    public record DatabaseAlreadyDeletingAndTerminated(Duration waitTime) implements DbDeleteResult {}
    public record DatabaseDeletedAndTerminated(Duration waitTime) implements DbDeleteResult {}
    public record DatabaseIllegallyNotFound() implements DbDeleteResult {}

    public record DbDeleteRequest(
        DbRef dbRef,
        boolean ifExists,
        boolean forceDelete,
        LongRunningOptions lrOptions,
        BiConsumer<String, UUID> assertShouldDelete
    ) {}

    @Override
    public DbDeleteResult execute() {
        val dbInfo = dbGateway.tryFindOne(request.dbRef);

        if (dbInfo.isEmpty() || dbInfo.get().getStatus() == TERMINATED) {
            return handleDbNotFound(request.ifExists);
        }

        if (dbInfo.get().getStatus() == TERMINATING) {
            return handleDbDeleted(request.dbRef, request.lrOptions, DatabaseAlreadyDeleting::new, DatabaseAlreadyDeletingAndTerminated::new);
        }

        if (!request.forceDelete) {
            request.assertShouldDelete.accept(dbInfo.get().getInfo().getName(), UUID.fromString(dbInfo.get().getId()));
        }

        val status = dbGateway.delete(request.dbRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleDbDeleted(request.dbRef, request.lrOptions, DatabaseDeleted::new, DatabaseDeletedAndTerminated::new);
            case DeletionStatus.NotFound<?> _ -> handleDbNotFound(request.ifExists);
        };
    }

    private DbDeleteResult handleDbDeleted(DbRef dbRef, LongRunningOptions lrOptions, Supplier<DbDeleteResult> deleted, Function<Duration, DbDeleteResult> deletedAndTerminated) {
        if (lrOptions.dontWait()) {
            return deleted.get();
        }

        val awaitedDuration = dbGateway.waitUntilDbStatus(dbRef, TERMINATED, lrOptions.timeout());
        return deletedAndTerminated.apply(awaitedDuration);
    }

    private DbDeleteResult handleDbNotFound(boolean ifExists) {
        if (ifExists) {
            return new DatabaseNotFound();
        } else {
            return new DatabaseIllegallyNotFound();
        }
    }
}
