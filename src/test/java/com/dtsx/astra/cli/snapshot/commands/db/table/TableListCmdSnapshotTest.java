package com.dtsx.astra.cli.snapshot.commands.db.table;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway.FoundKeyspaces;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;
import com.dtsx.astra.cli.testlib.Fixtures.Tables;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TableListCmdSnapshotTest extends BaseCmdSnapshotTest {

    private final SnapshotTestOptionsModifier foundWithKeyspace = (o) -> o
        .gateway(TableGateway.class, (mock) -> {
            when(mock.findAll(any())).thenReturn(Tables.Many);
        })
        .verify((mocks) -> {
            verify(mocks.tableGateway()).findAll(Databases.Keyspace);
        });

    private final SnapshotTestOptionsModifier foundWithAll = (o) -> o
        .gateway(KeyspaceGateway.class, (mock) -> {
            when(mock.findAll(any())).thenReturn(new FoundKeyspaces("default_keyspace", List.of("ks1", "ks2")));
        })
        .gateway(TableGateway.class, (mock) -> {
            when(mock.findAll(any())).thenReturn(Tables.Many);
        })
        .verify((mocks) -> {
            verify(mocks.keyspaceGateway()).findAll(Databases.NameRef);
            verify(mocks.tableGateway(), times(2)).findAll(any());
        });

    @TestForAllOutputs
    public void tables_found_using_keyspace(OutputType outputType) {
        verifyRun("db list-tables ${DatabaseName} -k ${Keyspace}", outputType, foundWithKeyspace);
    }

    @TestForDifferentOutputs
    public void tables_found_using_all(OutputType outputType) {
        verifyRun("db list-tables ${DatabaseName} --all", outputType, foundWithAll);
    }
}
