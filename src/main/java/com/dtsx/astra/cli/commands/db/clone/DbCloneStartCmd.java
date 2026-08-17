package com.dtsx.astra.cli.commands.db.clone;

import com.dtsx.astra.cli.core.CliConstants.$Db;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.WithSetTimeout;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.prompters.specific.DbRefPrompter;
import com.dtsx.astra.cli.core.output.prompters.specific.SnapshotPrompter;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.clone.DbCloneStartOperation;
import com.dtsx.astra.cli.operations.db.clone.DbCloneStartOperation.CloneCompleted;
import com.dtsx.astra.cli.operations.db.clone.DbCloneStartOperation.CloneStarted;
import com.dtsx.astra.cli.operations.db.clone.DbCloneStartOperation.DbCloneStartRequest;
import com.dtsx.astra.cli.operations.db.clone.DbCloneStartOperation.DbCloneStartResult;
import com.dtsx.astra.sdk.db.domain.DatabaseCloneStatus;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "start",
    aliases = { "create" },
    description = "Start a clone operation from an existing database snapshot"
)
@Example(
    comment = "Start and wait for a clone op interactively",
    command = "${cli.name} db clone start"
)
@Example( // TODO probably use --from/--to or --source/--target or something
    comment = "Start and wait for a clone op with explicit arguments",
    command = "${cli.name} db clone start my_new_db -sd my_old_db -s snap123"
)
@Example(
    comment = "Start a clone without waiting for it to become active",
    command = "${cli.name} db clone start my_new_db -sd my_old_db -s snap123 --async"
)
@Example(
    comment = "Start a clone from a source database in a specific region",
    command = "${cli.name} db clone start my_new_db -sd my_old_db -s snap123 -sr us-east1"
)
public class DbCloneStartCmd extends AbstractDbCloneCmd<DbCloneStartResult> implements WithSetTimeout {
    @Option(
        names = { "-sd", "--source-db" },
        description = "The source database to clone from",
        paramLabel = $Db.LABEL
    )
    public DbRef $sourceDbRef;

    @Option(
        names = { "-s", "--snapshot-id" },
        description = "The snapshot ID to clone from",
        paramLabel = "SNAPSHOT_ID"
    )
    public String $snapshotId;

    @Option(
        names = { "-sr", "--source-region" },
        description = "The region of the source database",
        paramLabel = $Regions.LABEL
    )
    public Optional<String> $sourceRegion = Optional.empty();

    @Mixin
    protected LongRunningOptionsMixin lrMixin;

    @Option(
        names = LR_OPTS_TIMEOUT_NAME,
        description = "Timeout for the clone operation to complete",
        defaultValue = "30m"
    )
    public void setTimeout(Duration timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        if ($sourceDbRef == null) {
            $sourceDbRef = DbRefPrompter.prompt(ctx, dbGateway, "Select the source database to clone from:", (b) -> b.fallbackIndex(0).fix(originalArgs(), "--source-db"));
        }
        
        if ($snapshotId == null) {
            $snapshotId = SnapshotPrompter.prompt(ctx, dbCloneGateway, $sourceDbRef, "Select the snapshot to clone from:", (b) -> b.fallbackIndex(0).fix(originalArgs(), "--snapshot-id"));
        }
    }

    @Override
    protected OutputJson executeJson(Supplier<DbCloneStartResult> result) {
        return switch (result.get()) {
            case CloneStarted(var status) -> OutputJson.serializeValue(mkData(status, null));
            case CloneCompleted(var status, var waitTime) -> OutputJson.serializeValue(mkData(status, waitTime));
        };
    }

    @Override
    protected OutputAll execute(Supplier<DbCloneStartResult> result) {
        return switch (result.get()) {
            case CloneStarted(var status) -> {
                val msg = "Clone operation started with ID %s.".formatted(
                    ctx.highlight(status.getOperationId())
                );
                yield OutputAll.response(msg, mkData(status, null), List.of(
                    new Hint("Check clone status", originalArgs(), "--operation-id " + status.getOperationId())
                ));
            }
            case CloneCompleted(var status, var waitTime) -> {
                val msg = "Clone operation completed after waiting %s seconds.".formatted(
                    ctx.highlight(waitTime.toSeconds())
                );
                yield OutputAll.response(msg, mkData(status, waitTime), List.of(
                    new Hint("Get database info", "${cli.name} db get %s".formatted($dbRef))
                ));
            }
        };
    }

    @Override
    protected Operation<DbCloneStartResult> mkOperation() {
        return new DbCloneStartOperation(dbCloneGateway, new DbCloneStartRequest(
            $dbRef, $sourceDbRef, $snapshotId, $sourceRegion, lrMixin.options(ctx)
        ));
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the target database";
    }

    private LinkedHashMap<String, Object> mkData(DatabaseCloneStatus status, Duration waitedDuration) {
        return sequencedMapOf(
            "operationId", status.getOperationId(),
            "status", status.getStatus(),
            "phase", status.getPhase(),
            "sourceDbId", status.getSourceDbId(),
            "targetDbId", status.getTargetDbId(),
            "snapshotId", status.getSnapshotId(),
            "waitedSeconds", Optional.ofNullable(waitedDuration).map(Duration::getSeconds) // TODO check if data is all correct and complete
        );
    }
}
