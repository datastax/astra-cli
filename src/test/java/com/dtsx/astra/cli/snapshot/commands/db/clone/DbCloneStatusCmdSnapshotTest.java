package com.dtsx.astra.cli.snapshot.commands.db.clone;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.clone.DbCloneGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Clone;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DbCloneStatusCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier opts() {
        return (o) -> o
            .gateway(DbCloneGateway.class, (mock) -> {
                doReturn(Clone.Status).when(mock).getCloneStatus(any(), any());
            });
    }

    @TestForAllOutputs
    public void db_clone_status(OutputType outputType) {
        verifyRun("db clone status ${DatabaseName} --operation-id clone-op-12345", outputType, o -> o.use(opts())
            .verify((mocks) -> {
                verify(mocks.dbCloneGateway()).getCloneStatus(Databases.NameRef, "clone-op-12345");
            }));
    }
}
