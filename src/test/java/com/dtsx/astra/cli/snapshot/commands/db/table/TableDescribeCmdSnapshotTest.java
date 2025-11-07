package com.dtsx.astra.cli.snapshot.commands.db.table;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Tables;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TableDescribeCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier tableFound = (o) -> o
        .gateway(TableGateway.class, (mock) -> {
            when(mock.findOne(any())).thenReturn(Optional.of(Tables.One));
        })
        .verify((mocks) -> {
            verify(mocks.tableGateway()).findOne(Tables.Ref);
        });

    private final SnapshotTestOptionsModifier tableNotFound = (o) -> o
        .gateway(TableGateway.class, (mock) -> {
            when(mock.findOne(any())).thenReturn(Optional.empty());
        })
        .verify((mocks) -> {
            verify(mocks.tableGateway()).findOne(Tables.Ref);
        });

    @TestForAllOutputs
    public void table_found(OutputType outputType) {
        verifyRun("db describe-table ${DatabaseName} -k default_keyspace -t ${TableName}", outputType, tableFound);
    }

    @TestForDifferentOutputs
    public void error_table_not_found(OutputType outputType) {
        verifyRun("db describe-table ${DatabaseName} -k default_keyspace -t ${TableName}", outputType, tableNotFound);
    }
}

