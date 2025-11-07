package com.dtsx.astra.cli.snapshot.commands.db.table;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Tables;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TableDeleteCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier deleteTable(Function<TableRef, DeletionStatus<TableRef>> lift) {
        return (o) -> o
            .gateway(TableGateway.class, (mock) -> {
                doReturn(lift.apply(Tables.Ref)).when(mock).delete(any());
            })
            .verify((mocks) -> {
                verify(mocks.tableGateway()).delete(Tables.Ref);
            });
    }

    @TestForAllOutputs
    public void table_deleted(OutputType outputType) {
        verifyRun("db delete-table ${DatabaseName} -k default_keyspace -t ${TableName}", outputType, deleteTable(DeletionStatus::deleted));
    }

    @TestForDifferentOutputs
    public void error_table_not_found(OutputType outputType) {
        verifyRun("db delete-table ${DatabaseName} -k default_keyspace -t ${TableName}", outputType, deleteTable(DeletionStatus::notFound));
    }

    @TestForDifferentOutputs
    public void allow_table_not_found(OutputType outputType) {
        verifyRun("db delete-table ${DatabaseName} -k default_keyspace -t ${TableName} --if-exists", outputType, deleteTable(DeletionStatus::notFound));
    }
}
