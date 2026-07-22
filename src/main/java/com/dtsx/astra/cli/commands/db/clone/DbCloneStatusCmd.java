package com.dtsx.astra.cli.commands.db.clone;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.clone.DbCloneStatusOperation;
import com.dtsx.astra.cli.operations.db.clone.DbCloneStatusOperation.DbCloneStatusRequest;
import com.dtsx.astra.sdk.db.domain.DatabaseCloneStatus;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "status",
    description = "Check the status of an ongoing or completed clone operation"
)
@Example(
    comment = "Check the status of a specific clone operation",
    command = "${cli.name} db clone status my_db -o clone-op-12345"
)
public class DbCloneStatusCmd extends AbstractDbCloneCmd<DatabaseCloneStatus> {
    @Option(
        names = { "--operation-id" },
        description = "The ID of the clone operation",
        paramLabel = "OPERATION_ID"
    )
    public String $operationId;

    @Override
    protected OutputJson executeJson(Supplier<DatabaseCloneStatus> result) {
        return OutputJson.serializeValue(result);
    }

    @Override
    protected OutputAll execute(Supplier<DatabaseCloneStatus> result) {
        val status = result.get();
        return ShellTable.forAttributes(sequencedMapOf(
            "Operation ID", status.getOperationId(),
            "Status", status.getStatus(),
            "Source DB", status.getSourceDbId(),
            "Target DB", status.getTargetDbId(),
            "Snapshot ID", status.getSnapshotId(),
            "Phase", status.getPhase(),
            "Message", status.getMessage()
        ));
    }

    @Override
    protected Operation<DatabaseCloneStatus> mkOperation() {
        return new DbCloneStatusOperation(dbCloneGateway, new DbCloneStatusRequest($dbRef, $operationId));
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the target database for the clone operation";
    }
}
