package com.dtsx.astra.cli.snapshot.commands.db;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;
import com.dtsx.astra.sdk.db.domain.Database;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DbListCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier mkOpts(Stream<Database> ret) {
        return (o) -> o
            .gateway(DbGateway.class, (mock) -> {
                when(mock.findAll()).thenReturn(ret);
            })
            .verify((mocks) -> {
                verify(mocks.dbGateway()).findAll();
            });
    }

    @TestForAllOutputs
    public void dbs_found(OutputType outputType) {
        verifyRun("db", outputType, mkOpts(Databases.Many.stream()));
    }

    @TestForAllOutputs
    public void no_dbs_found(OutputType outputType) {
        verifyRun("db list", outputType, mkOpts(Stream.of()));
    }
}
