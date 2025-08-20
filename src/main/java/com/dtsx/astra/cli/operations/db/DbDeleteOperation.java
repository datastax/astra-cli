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

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.cli.operations.db.DbDeleteOperation.DbDeleteResult;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.TERMINATED;

@RequiredArgsConstructor
public class DbDeleteOperation implements Operation<DbDeleteResult> {
    private final DbGateway dbGateway;
    private final DbDeleteRequest request;

    public sealed interface DbDeleteResult {}
    public record DatabaseNotFound() implements DbDeleteResult {}
    public record DatabaseDeleted() implements DbDeleteResult {}
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
        if (!request.forceDelete) {
            val dbInfo = dbGateway.tryFindOneDb(request.dbRef);

            if (dbInfo.isEmpty()) {
                return handleDbNotFound(request.ifExists);
            }

            request.assertShouldDelete.accept(dbInfo.get().getInfo().getName(), UUID.fromString(dbInfo.get().getId()));
        }

        val status = dbGateway.deleteDb(request.dbRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleDbDeleted(request.dbRef, request.lrOptions);
            case DeletionStatus.NotFound<?> _ -> handleDbNotFound(request.ifExists);
        };
    }

    private DbDeleteResult handleDbDeleted(DbRef dbRef, LongRunningOptions lrOptions) {
        if (lrOptions.dontWait()) {
            return new DatabaseDeleted();
        }

        val awaitedDuration = dbGateway.waitUntilDbStatus(dbRef, TERMINATED, lrOptions.timeout());
        return new DatabaseDeletedAndTerminated(awaitedDuration);
    }

    private DbDeleteResult handleDbNotFound(boolean ifExists) {
        if (ifExists) {
            return new DatabaseNotFound();
        } else {
            return new DatabaseIllegallyNotFound();
        }
    }
}
