package com.dtsx.astra.cli.snapshot.commands.db.clone;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.clone.DbCloneGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Clone;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class DbCloneStatusCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier opts() {
        return (o) -> o
            .gateway(DbCloneGateway.class, (mock) -> {
                doReturn(Clone.Status).when(mock).findClone(any(), any());
            });
    }

    @TestForAllOutputs
    public void db_clone_status(OutputType outputType) {
        verifyRun("db clone status ${DatabaseName} -id ${CloneOperationId}", outputType, o -> o.use(opts())
            .verify((mocks) -> {
                verify(mocks.dbCloneGateway()).findClone(Databases.NameRef, Clone.OperationId);
            }));
    }
}
