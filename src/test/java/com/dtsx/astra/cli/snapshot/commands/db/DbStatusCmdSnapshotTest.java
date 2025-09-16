package com.dtsx.astra.cli.snapshot.commands.db;

import com.dtsx.astra.cli.core.exceptions.internal.db.DbNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DbStatusCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier foundDbOpts = (o) -> o
        .gateway(DbGateway.class, (mock) -> {
            when(mock.findOne(any())).thenReturn(Databases.One);
        })
        .verify((mocks) -> {
            verify(mocks.dbGateway()).findOne(Databases.IdRef);
        });

    private final SnapshotTestOptionsModifier notFoundDbOpts = (o) -> o
        .gateway(DbGateway.class, (mock) -> {
            when(mock.findOne(any())).thenThrow(new DbNotFoundException(DbRef.fromNameUnsafe("*nonexistent*")));
        })
        .verify((mocks) -> {
            verify(mocks.dbGateway()).findOne(DbRef.fromNameUnsafe("*nonexistent*"));
        });

    @TestForAllOutputs
    public void db_status_found(OutputType outputType) {
        verifyRun("db status ${DatabaseId}", outputType, foundDbOpts);
    }

    @TestForAllOutputs
    public void error_db_not_found(OutputType outputType) {
        verifyRun("db status *nonexistent*", outputType, notFoundDbOpts);
    }
}
