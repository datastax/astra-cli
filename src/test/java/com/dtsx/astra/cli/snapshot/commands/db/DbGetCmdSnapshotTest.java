package com.dtsx.astra.cli.snapshot.commands.db;

import com.dtsx.astra.cli.core.exceptions.internal.db.DbNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DbGetCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier foundDbOpts = (o) -> o
        .gateway(DbGateway.class, (mock) -> {
            when(mock.findOne(any())).thenReturn(Fixtures.Database);
        })
        .verify((mocks) -> {
            verify(mocks.dbGateway()).findOne(Fixtures.DatabaseName);
        });

    private final SnapshotTestOptionsModifier notFoundDbOpts = (o) -> o
        .gateway(DbGateway.class, (mock) -> {
            when(mock.findOne(any())).thenThrow(new DbNotFoundException(DbRef.fromNameUnsafe("*whatever*")));
        })
        .verify((mocks) -> {
            verify(mocks.dbGateway()).findOne(DbRef.fromNameUnsafe("*whatever*"));
        });

    @TestForAllOutputs
    public void db_full_info(OutputType outputType) {
        verifyRun("db get ${DatabaseName}", outputType, foundDbOpts);
    }

    @TestForAllOutputs
    public void db_partial_info(OutputType outputType) {
        verifyRun("db get ${DatabaseName} --key keyspaces", outputType, foundDbOpts);
    }

    @TestForAllOutputs
    public void db_not_found(OutputType outputType) {
        verifyRun("db get *whatever*", outputType, notFoundDbOpts);
    }
}
