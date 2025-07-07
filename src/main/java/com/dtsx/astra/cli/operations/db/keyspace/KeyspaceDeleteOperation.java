package com.dtsx.astra.cli.operations.db.keyspace;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.cli.operations.db.keyspace.KeyspaceDeleteOperation.*;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;

@RequiredArgsConstructor
public class KeyspaceDeleteOperation implements Operation<KeyspaceDeleteResult> {
    private final KeyspaceGateway keyspaceGateway;
    private final DbGateway dbGateway;
    private final KeyspaceDeleteRequest request;

    public sealed interface KeyspaceDeleteResult {}
    public record KeyspaceNotFound() implements KeyspaceDeleteResult {}
    public record KeyspaceDeleted() implements KeyspaceDeleteResult {}
    public record KeyspaceDeletedAndDbActive(Duration waitTime) implements KeyspaceDeleteResult {}
    public record KeyspaceIllegallyNotFound() implements KeyspaceDeleteResult {}

    public record KeyspaceDeleteRequest(
        KeyspaceRef keyspaceRef,
        boolean ifExists,
        LongRunningOptions lrOptions
    ) {}

    @Override
    public KeyspaceDeleteResult execute() {
        val status = keyspaceGateway.delete(request.keyspaceRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleKsDeleted(request.keyspaceRef.db(), request.lrOptions);
            case DeletionStatus.NotFound<?> _ -> handleKsNotFound(request.ifExists);
        };
    }

    private KeyspaceDeleteResult handleKsDeleted(DbRef dbRef, LongRunningOptions lrOptions) {
        if (lrOptions.dontWait()) {
            return new KeyspaceDeleted();
        }

        val awaitedDuration = dbGateway.waitUntilDbStatus(dbRef, ACTIVE, lrOptions.timeout());
        return new KeyspaceDeletedAndDbActive(awaitedDuration);
    }

    private KeyspaceDeleteResult handleKsNotFound(boolean ifExists) {
        if (ifExists) {
            return new KeyspaceNotFound();
        } else {
            return new KeyspaceIllegallyNotFound();
        }
    }

}
