package com.dtsx.astra.cli.snapshot.commands.db.keyspace;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForHumanOutput;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;

import java.time.Duration;
import java.util.function.Function;

import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class KeyspaceCreateCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier opts(Function<KeyspaceRef, CreationStatus<KeyspaceRef>> lift) {
        return (o) -> o
            .gateway(KeyspaceGateway.class, (mock) -> {
                doReturn(lift.apply(Databases.Keyspace)).when(mock).create(any());
            })
            .gateway(DbGateway.class, (mock) -> {
                when(mock.waitUntilDbStatus(any(), any(), any())).thenReturn(Duration.ofMillis(9876));
            })
            .verify((mocks) -> {
                mocks.keyspaceGateway().create(Databases.Keyspace);
            });
    }

    @TestForAllOutputs
    public void keyspace_created_and_db_active(OutputType outputType) {
        verifyRun("db create-keyspace ${DatabaseName} -k ${Keyspace}", outputType, o -> o.use(opts(CreationStatus::created))
            .verify((mocks) -> {
                verify(mocks.dbGateway()).waitUntilDbStatus(Databases.NameRef, ACTIVE, Duration.ofSeconds(60));
            }));
    }

    @TestForHumanOutput
    public void keyspace_created_async(OutputType outputType) {
        verifyRun("db create-keyspace ${DatabaseName} -k ${Keyspace} --async", outputType, o -> o.use(opts(CreationStatus::created))
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).waitUntilDbStatus(any(), any(), any());
            }));
    }

    @TestForHumanOutput
    public void error_keyspace_exists(OutputType outputType) {
        verifyRun("db create-keyspace ${DatabaseName} -k ${Keyspace}", outputType, o -> o.use(opts(CreationStatus::alreadyExists))
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).waitUntilDbStatus(any(), any(), any());
            }));
    }

    @TestForHumanOutput
    public void allow_keyspace_exists(OutputType outputType) {
        verifyRun("db create-keyspace ${DatabaseName} -k ${Keyspace} --if-not-exists", outputType, o -> o.use(opts(CreationStatus::alreadyExists))
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).waitUntilDbStatus(any(), any(), any());
            }));
    }
}
