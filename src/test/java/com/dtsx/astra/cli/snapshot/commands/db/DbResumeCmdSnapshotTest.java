package com.dtsx.astra.cli.snapshot.commands.db;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;
import com.dtsx.astra.cli.utils.JsonUtils;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.val;
import org.graalvm.collections.Pair;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DbResumeCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier resumeDbOpts(DatabaseStatusType initialStatus, Duration waitedTime) {
        return (o) -> o
            .gateway(DbGateway.class, (mock) -> {
                when(mock.resume(any(), any())).thenReturn(Pair.create(initialStatus, waitedTime));
            })
            .verify((mocks) -> {
                verify(mocks.dbGateway()).resume(Databases.NameRef, Optional.of(Duration.ofSeconds(600)));
            });
    }

    private SnapshotTestOptionsModifier resumeDbAsyncOpts(DatabaseStatusType initialStatus) {
        return (o) -> o
            .gateway(DbGateway.class, (mock) -> {
                when(mock.resume(any(), any())).thenReturn(Pair.create(initialStatus, Duration.ZERO));

                val copiedDb = JsonUtils.clone(Databases.One, Database.class);
                copiedDb.setStatus(initialStatus);

                when(mock.findOne(any())).thenReturn(copiedDb);
            })
            .verify((mocks) -> {
                verify(mocks.dbGateway()).resume(Databases.IdRef, Optional.empty());

                if (initialStatus != DatabaseStatusType.ACTIVE) {
                    verify(mocks.dbGateway()).findOne(Databases.IdRef);
                }
            });
    }

    @TestForAllOutputs
    public void resume_creating_db(OutputType outputType) {
        verifyRun("db resume ${DatabaseName}", outputType, resumeDbOpts(DatabaseStatusType.INITIALIZING, Duration.ofSeconds(100)));
    }

    @TestForDifferentOutputs
    public void resume_hibernated_db(OutputType outputType) {
        verifyRun("db resume ${DatabaseName}", outputType, resumeDbOpts(DatabaseStatusType.HIBERNATED, Duration.ofSeconds(100)));
    }

    @TestForDifferentOutputs
    public void resume_active_db(OutputType outputType) {
        verifyRun("db resume ${DatabaseName}", outputType, resumeDbOpts(DatabaseStatusType.ACTIVE, Duration.ZERO));
    }

    @TestForAllOutputs
    public void resume_creating_db_async(OutputType outputType) {
        verifyRun("db resume ${DatabaseId} --async", outputType, resumeDbAsyncOpts(DatabaseStatusType.INITIALIZING));
    }

    @TestForDifferentOutputs
    public void resume_hibernated_db_async(OutputType outputType) {
        verifyRun("db resume ${DatabaseId} --async", outputType, resumeDbAsyncOpts(DatabaseStatusType.HIBERNATED));
    }

    @TestForDifferentOutputs
    public void resume_active_db_async(OutputType outputType) {
        verifyRun("db resume ${DatabaseId} --async", outputType, resumeDbAsyncOpts(DatabaseStatusType.ACTIVE));
    }
}
