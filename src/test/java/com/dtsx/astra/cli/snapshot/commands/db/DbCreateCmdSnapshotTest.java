package com.dtsx.astra.cli.snapshot.commands.db;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;
import com.dtsx.astra.sdk.db.domain.Database;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class DbCreateCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier opts(Function<Database, CreationStatus<Database>> lift, boolean allowDuplicates) {
        return (o) -> o
            .gateway(DbGateway.class, (mock) -> {
                when(mock.findCloudForRegion(any(), any(), anyBoolean())).thenReturn(CloudProvider.AWS);

                doReturn(lift.apply(Databases.One)).when(mock).create(any(), any(), any(), any(), any(), anyInt(), anyBoolean(), anyBoolean());

                when(mock.waitUntilDbStatus(any(), any(), any())).thenReturn(Duration.ofMillis(6789));

                when(mock.resume(any(), any())).thenReturn(Pair.of(ACTIVE, Duration.ZERO));
            })
            .verify((mocks) -> {
                verify(mocks.dbGateway()).findCloudForRegion(Optional.empty(), RegionName.mkUnsafe("us-east-1"), true);

                verify(mocks.dbGateway()).create(Databases.NameRef.toString(), "default_keyspace", RegionName.mkUnsafe("us-east-1"), CloudProvider.AWS, "serverless", 1, true, allowDuplicates);
            });
    }

    @TestForAllOutputs
    public void db_create(OutputType outputType) {
        verifyRun("db create ${DatabaseName} -r us-east-1", outputType, o -> o.use(opts(CreationStatus::created, false))
            .verify((mocks) -> {
                verify(mocks.dbGateway()).waitUntilDbStatus(Databases.IdRef, ACTIVE, Duration.ofSeconds(600));
            }));
    }

    @TestForDifferentOutputs
    public void db_create_allowing_duplicates(OutputType outputType) {
        verifyRun("db create ${DatabaseName} -r us-east-1 --allow-duplicate-names", outputType, o -> o.use(opts(CreationStatus::created, true))
            .verify((mocks) -> {
                verify(mocks.dbGateway()).waitUntilDbStatus(Databases.IdRef, ACTIVE, Duration.ofSeconds(600));
            }));
    }

    @TestForAllOutputs
    public void db_create_async(OutputType outputType) {
        verifyRun("db create ${DatabaseName} -r us-east-1 --async", outputType, o -> o.use(opts(CreationStatus::created, false))
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).waitUntilDbStatus(any(), any(), any());
                verify(mocks.dbGateway(), never()).resume(any(), any());
            }));
    }

    @TestForDifferentOutputs
    public void error_db_already_exists(OutputType outputType) {
        verifyRun("db create ${DatabaseName} -r us-east-1", outputType, o -> o.use(opts(CreationStatus::alreadyExists, false))
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).waitUntilDbStatus(any(), any(), any());
                verify(mocks.dbGateway(), never()).resume(any(), any());
            }));
    }

    @TestForDifferentOutputs
    public void connect_to_existing_db(OutputType outputType) {
        verifyRun("db create ${DatabaseName} -r us-east-1 --if-not-exists", outputType, o -> o.use(opts(CreationStatus::alreadyExists, false))
            .verify((mocks) -> {
                verify(mocks.dbGateway()).resume(Databases.IdRef, Optional.of(Duration.ofSeconds(600)));
            }));
    }

    @TestForDifferentOutputs
    public void allow_existing_db_async(OutputType outputType) {
        verifyRun("db create ${DatabaseName} -r us-east-1 --if-not-exists --async", outputType, o -> o.use(opts(CreationStatus::alreadyExists, false))
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).waitUntilDbStatus(any(), any(), any());
                verify(mocks.dbGateway(), never()).resume(any(), any());
            }));
    }
}
