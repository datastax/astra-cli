package com.dtsx.astra.cli.snapshot.commands.db.region;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.internal.db.DbNotFoundException;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;
import com.dtsx.astra.cli.testlib.Fixtures.Regions;

import java.time.Duration;
import java.util.function.Function;

import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class RegionDeleteCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier notFoundDb = (o) -> o
        .gateway(RegionGateway.class, (mock) -> {
            doThrow(new DbNotFoundException(Databases.IdRef)).when(mock).delete(any(), any());
        })
        .verify((mocks) -> {
            verify(mocks.regionGateway()).delete(any(), any());
        });

    private SnapshotTestOptionsModifier deleteRegion(Function<RegionName, DeletionStatus<RegionName>> lift) {
        return (o) -> o
            .gateway(RegionGateway.class, (mock) -> {
                doReturn(lift.apply(Regions.NAME)).when(mock).delete(any(), any());
            })
            .gateway(DbGateway.class, (mock) -> {
                when(mock.waitUntilDbStatus(any(), any(), anyInt())).thenReturn(Duration.ofMillis(6789));
            })
            .verify((mocks) -> {
                verify(mocks.regionGateway()).delete(Databases.IdRef, Regions.NAME);
            });
    }

    @TestForAllOutputs
    public void region_deleted(OutputType outputType) {
        verifyRun("db delete-region ${DatabaseId} -r ${RegionName}", outputType, o -> o.use(deleteRegion(DeletionStatus::deleted))
            .verify((mocks) -> {
                verify(mocks.dbGateway()).waitUntilDbStatus(Databases.IdRef, ACTIVE, 600);
            }));
    }

    @TestForDifferentOutputs
    public void region_delete_async(OutputType outputType) {
        verifyRun("db delete-region ${DatabaseId} -r ${RegionName} --async", outputType, o -> o.use(deleteRegion(DeletionStatus::deleted))
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).waitUntilDbStatus(any(), any(), anyInt());
            }));
    }

    @TestForDifferentOutputs
    public void error_db_not_found(OutputType outputType) {
        verifyRun("db delete-region ${DatabaseId} -r ${RegionName}", outputType, notFoundDb);
    }
    
    @TestForDifferentOutputs
    public void error_region_not_found(OutputType outputType) {
        verifyRun("db delete-region ${DatabaseId} -r ${RegionName}", outputType, o -> o.use(deleteRegion(DeletionStatus::notFound))
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).waitUntilDbStatus(any(), any(), anyInt());
            }));
    }

    @TestForDifferentOutputs
    public void allow_missing_region(OutputType outputType) {
        verifyRun("db delete-region ${DatabaseId} -r ${RegionName} --if-exists --async", outputType, o -> o.use(deleteRegion(DeletionStatus::notFound))
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).waitUntilDbStatus(any(), any(), anyInt());
            }));
    }
}
