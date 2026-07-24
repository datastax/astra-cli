package com.dtsx.astra.cli.snapshot.commands.db.clone;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.clone.DbCloneGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Clone;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

public class DbCloneListSnapshotsCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier opts() {
        return (o) -> o
            .gateway(DbCloneGateway.class, (mock) -> {
                doAnswer(inv -> Clone.Snapshots.stream()).when(mock).findSnapshots(any(), any(), any(), any());
            });
    }

    @TestForAllOutputs
    public void db_clone_list_snapshots(OutputType outputType) {
        verifyRun("db clone list-snapshots ${DatabaseName}", outputType, o -> o.use(opts())
            .verify((mocks) -> {
                verify(mocks.dbCloneGateway()).findSnapshots(Databases.NameRef, Optional.empty(), Optional.empty(), Optional.empty());
            }));
    }
}
