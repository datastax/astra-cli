package com.dtsx.astra.cli.snapshot.commands.db.table;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Tables;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TableTruncateCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier tableTruncated = (o) -> o
        .gateway(TableGateway.class, (mock) -> {
            doReturn(DeletionStatus.deleted(Tables.Ref.name())).when(mock).truncate(any());
        })
        .verify((mocks) -> {
            verify(mocks.tableGateway()).truncate(Tables.Ref);
        });

    private final SnapshotTestOptionsModifier tableNotFound = (o) -> o
        .gateway(TableGateway.class, (mock) -> {
            doReturn(DeletionStatus.notFound(Tables.Ref.name())).when(mock).truncate(any());
        })
        .verify((mocks) -> {
            verify(mocks.tableGateway()).truncate(Tables.Ref);
        });

    @TestForAllOutputs
    public void table_truncated(OutputType outputType) {
        verifyRun("db truncate-table ${DatabaseName} -t ${TableName}", outputType, tableTruncated);
    }

    @TestForDifferentOutputs
    public void error_table_not_found(OutputType outputType) {
        verifyRun("db truncate-table ${DatabaseName} -t ${TableName}", outputType, tableNotFound);
    }
}
