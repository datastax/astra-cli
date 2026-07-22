package com.dtsx.astra.cli.gateways.db.clone;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.sdk.db.domain.DatabaseCloneStatus;
import com.dtsx.astra.sdk.db.domain.DatabaseSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.awaitGenericStatus;
import static com.dtsx.astra.cli.core.output.ExitCode.STATUS_ISSUE;

@RequiredArgsConstructor
public class DbCloneGatewayImpl implements DbCloneGateway {
    private final CliContext ctx;
    private final APIProvider api;
    private final DbGateway dbGateway;

    @Override
    public DatabaseCloneStatus cloneFrom(DbRef targetDbRef, DbRef sourceDbRef, String snapshotId, Optional<String> sourceRegion) {
        val sourceId = dbGateway.findOne(targetDbRef).getId();

        return api.dbOpsClient(targetDbRef).clone().cloneFrom(
            sourceId,
            snapshotId,
            sourceRegion.orElse(null)
        );
    }

    @Override
    public DatabaseCloneStatus getCloneStatus(DbRef targetDbRef, String operationId) {
        return api.dbOpsClient(targetDbRef).clone().getCloneStatus(operationId);
    }

    @Override
    public Duration waitUntilCloneStatus(DbRef targetDbRef, String operationId, String targetStatus, Duration timeout) {
        return awaitGenericStatus(
            ctx,
            "database clone",
            targetStatus.toUpperCase(),
            () -> {
                val status = getCloneStatus(targetDbRef, operationId); // TODO awaitGenericStatus needs to take an 'invalid statuses' list
                if ("ERROR".equalsIgnoreCase(status.getStatus())) {
                    throw new AstraCliException(STATUS_ISSUE, "Awaiting database clone failed with status: " + status.getStatus() + ". Message: " + status.getMessage());
                }
                return status.getStatus().toUpperCase();
            },
            s -> s,
            timeout
        );
    }

    @Override
    public Stream<DatabaseSnapshot> findSnapshots(DbRef sourceDbRef, Optional<String> region, Optional<String> from, Optional<String> to) {
        return api.dbOpsClient(sourceDbRef).snapshots().find(
            from.orElse(null),
            to.orElse(null),
            region.orElse(null)
        );
    }
}
