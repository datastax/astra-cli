package com.dtsx.astra.cli.snapshot.commands.db.clone;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.clone.DbCloneGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Clone;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DbCloneStartCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier opts() {
        return (o) -> o
            .gateway(DbCloneGateway.class, (mock) -> {
                doReturn(Clone.Status).when(mock).cloneFrom(any(), any(), any(), any());
                when(mock.waitUntilClonePhase(any(), any(), any(), any())).thenReturn(Duration.ofMillis(6789));
                when(mock.findClone(any(), any())).thenReturn(Clone.StatusCompleted);
            });
    }

    @TestForAllOutputs
    public void db_clone_start(OutputType outputType) {
        verifyRun("db clone start new_db -sd ${DatabaseName} -s snap-789", outputType, o -> o.use(opts())
            .verify((mocks) -> {
                verify(mocks.dbCloneGateway()).cloneFrom(DbRef.fromNameUnsafe("new_db"), Databases.NameRef, "snap-789", Optional.empty());
                verify(mocks.dbCloneGateway()).waitUntilClonePhase(DbRef.fromNameUnsafe("new_db"), Clone.OperationId, "Done", Duration.ofMinutes(30));
            }));
    }

    @TestForAllOutputs
    public void db_clone_start_async(OutputType outputType) {
        verifyRun("db clone start new_db -sd ${DatabaseName} -s snap-789 --async", outputType, o -> o.use(opts())
            .verify((mocks) -> {
                verify(mocks.dbCloneGateway()).cloneFrom(DbRef.fromNameUnsafe("new_db"), Databases.NameRef, "snap-789", Optional.empty());
                verify(mocks.dbCloneGateway(), never()).waitUntilClonePhase(any(), any(), any(), any());
            }));
    }
}
