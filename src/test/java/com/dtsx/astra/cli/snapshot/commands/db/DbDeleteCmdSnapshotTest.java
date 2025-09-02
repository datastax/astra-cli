package com.dtsx.astra.cli.snapshot.commands.db;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForHumanOutput;
import com.dtsx.astra.cli.testlib.Fixtures;

import java.time.Duration;
import java.util.Optional;

import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.TERMINATED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DbDeleteCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier deleteDbOpts = (o) -> o
        .gateway(DbGateway.class, (mock) -> {
            when(mock.tryFindOne(any())).thenReturn(Optional.of(Fixtures.Database));

            doReturn(DeletionStatus.deleted(Fixtures.DatabaseName)).when(mock).delete(any()); // can't use when...then because of sealed interface

            when(mock.waitUntilDbStatus(any(), any(), anyInt())).thenReturn(Duration.ofMillis(6789));
        });

    @TestForAllOutputs
    public void db_force_delete(OutputType outputType) {
        verifyRun("db delete ${DatabaseName} --yes", outputType, o -> o.use(deleteDbOpts)
            .verify((mocks) -> {
                verify(mocks.dbGateway()).delete(Fixtures.DatabaseName);
            }));
    }

    @TestForAllOutputs
    public void error_when_no_confirmation(OutputType outputType) {
        verifyRun("db delete ${DatabaseName}", outputType, o -> o.use(deleteDbOpts)
            .verify((mocks) -> {
                verify(mocks.dbGateway()).tryFindOne(Fixtures.DatabaseName);
            }));
    }

    @TestForHumanOutput
    public void delete_db_with_confirmation(OutputType outputType) {
        verifyRun("db delete ${DatabaseName} --timeout 36", outputType, o -> o.use(deleteDbOpts)
            .stdin(Fixtures.DatabaseName.toString())
            .verify((mocks) -> {
                verify(mocks.dbGateway()).tryFindOne(Fixtures.DatabaseName);

                verify(mocks.dbGateway()).delete(Fixtures.DatabaseName);

                verify(mocks.dbGateway()).waitUntilDbStatus(Fixtures.DatabaseName, TERMINATED, 36);
            }));
    }

    @TestForHumanOutput
    public void error_when_invalid_confirmation(OutputType outputType) {
        verifyRun("db delete ${DatabaseName} --timeout 36", outputType, o -> o.use(deleteDbOpts)
            .stdin("*invalid*")
            .verify((mocks) -> {
                verify(mocks.dbGateway()).tryFindOne(Fixtures.DatabaseName);
            }));
    }
}
