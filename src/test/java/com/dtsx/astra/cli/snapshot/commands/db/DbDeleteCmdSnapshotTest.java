package com.dtsx.astra.cli.snapshot.commands.db;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForHumanOutput;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;

import java.time.Duration;
import java.util.Optional;

import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.TERMINATED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DbDeleteCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier deleteDbOpts = (o) -> o
        .gateway(DbGateway.class, (mock) -> {
            when(mock.tryFindOne(any())).thenReturn(Optional.of(Databases.One));

            doReturn(DeletionStatus.deleted(Databases.NameRef)).when(mock).delete(any()); // can't use when...then because of sealed interface

            when(mock.waitUntilDbStatus(any(), any(), anyInt())).thenReturn(Duration.ofMillis(6789));
        });

    private final SnapshotTestOptionsModifier dbNotFound = (o) -> o
        .gateway(DbGateway.class, (mock) -> {
            when(mock.tryFindOne(any())).thenReturn(Optional.empty());

            doReturn(DeletionStatus.notFound(Databases.NameRef)).when(mock).delete(any()); // can't use when...then because of sealed interface
        });

    @TestForAllOutputs
    public void db_force_delete(OutputType outputType) {
        verifyRun("db delete ${DatabaseName} --yes", outputType, o -> o.use(deleteDbOpts)
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).tryFindOne(any());

                verify(mocks.dbGateway()).delete(Databases.NameRef);

                verify(mocks.dbGateway()).waitUntilDbStatus(Databases.NameRef, TERMINATED, 800);
            }));
    }

    @TestForDifferentOutputs
    public void db_force_delete_async(OutputType outputType) {
        verifyRun("db delete ${DatabaseName} --yes --async", outputType, o -> o.use(deleteDbOpts)
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).tryFindOne(any());

                verify(mocks.dbGateway()).delete(Databases.NameRef);

                verify(mocks.dbGateway(), never()).waitUntilDbStatus(any(), any(), anyInt());
            }));
    }

    @TestForAllOutputs
    public void error_when_no_confirmation(OutputType outputType) {
        verifyRun("db delete ${DatabaseName}", outputType, o -> o.use(deleteDbOpts)
            .verify((mocks) -> {
                verify(mocks.dbGateway()).tryFindOne(Databases.NameRef);

                verify(mocks.dbGateway(), never()).delete(any());
            }));
    }

    @TestForAllOutputs
    public void delete_db_with_confirmation(OutputType outputType) {
        verifyRun("db delete ${DatabaseName} --timeout 36", outputType, o -> o.use(deleteDbOpts)
            .stdin(Databases.NameRef.toString())
            .verify((mocks) -> {
                verify(mocks.dbGateway()).tryFindOne(Databases.NameRef);

                verify(mocks.dbGateway()).delete(Databases.NameRef);

                verify(mocks.dbGateway()).waitUntilDbStatus(Databases.NameRef, TERMINATED, 36);
            }));
    }

    @TestForDifferentOutputs
    public void error_when_invalid_confirmation(OutputType outputType) {
        verifyRun("db delete ${DatabaseName} --timeout 36", outputType, o -> o.use(deleteDbOpts)
            .stdin("*invalid*")
            .verify((mocks) -> {
                verify(mocks.dbGateway()).tryFindOne(Databases.NameRef);

                verify(mocks.dbGateway(), never()).delete(any());
            }));
    }

    @TestForDifferentOutputs
    public void error_db_not_found_with_confirmation(OutputType outputType) {
        verifyRun("db delete *nonexistent* --yes", outputType, o -> o.use(dbNotFound)
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).tryFindOne(any());

                verify(mocks.dbGateway()).delete(DbRef.fromNameUnsafe("*nonexistent*"));
            }));
    }

    @TestForHumanOutput
    public void error_db_not_found_no_confirmation(OutputType outputType) {
        verifyRun("db delete *nonexistent*", outputType, o -> o.use(dbNotFound)
            .verify((mocks) -> {
                verify(mocks.dbGateway()).tryFindOne(DbRef.fromNameUnsafe("*nonexistent*"));

                verify(mocks.dbGateway(), never()).delete(any());
            }));
    }

    @TestForDifferentOutputs
    public void allow_db_not_found(OutputType outputType) {
        verifyRun("db delete *nonexistent* --yes --if-exists", outputType, o -> o.use(dbNotFound)
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).tryFindOne(any());

                verify(mocks.dbGateway()).delete(DbRef.fromNameUnsafe("*nonexistent*"));
            }));
    }
}
