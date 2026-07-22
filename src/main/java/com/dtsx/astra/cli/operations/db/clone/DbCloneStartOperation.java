package com.dtsx.astra.cli.operations.db.clone;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.clone.DbCloneGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.DatabaseCloneStatus;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.Optional;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.cli.operations.db.clone.DbCloneStartOperation.DbCloneStartResult;

@RequiredArgsConstructor
public class DbCloneStartOperation implements Operation<DbCloneStartResult> {
    private final DbCloneGateway dbCloneGateway;
    private final DbCloneStartRequest request;

    public record DbCloneStartRequest(
        DbRef targetDbRef,
        DbRef sourceDbRef,
        String snapshotId,
        Optional<String> sourceRegion,
        LongRunningOptions lrOptions
    ) {}

    public sealed interface DbCloneStartResult {}
    public record CloneStarted(DatabaseCloneStatus status) implements DbCloneStartResult {}
    public record CloneCompleted(DatabaseCloneStatus status, Duration waitTime) implements DbCloneStartResult {}

    @Override
    public DbCloneStartResult execute() {
        val status = dbCloneGateway.cloneFrom(
            request.targetDbRef,
            request.sourceDbRef,
            request.snapshotId,
            request.sourceRegion
        );

        if (request.lrOptions.dontWait()) {
            return new CloneStarted(status);
        }

        val waitTime = dbCloneGateway.waitUntilCloneStatus(
            request.targetDbRef,
            status.getOperationId(),
            "DONE",
            request.lrOptions.timeout()
        );

        return new CloneCompleted(dbCloneGateway.getCloneStatus(request.targetDbRef, status.getOperationId()), waitTime);
    }
}
