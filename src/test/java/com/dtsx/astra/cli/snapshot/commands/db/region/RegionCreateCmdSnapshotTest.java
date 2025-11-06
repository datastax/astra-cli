package com.dtsx.astra.cli.snapshot.commands.db.region;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
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
import static org.mockito.Mockito.*;

public class RegionCreateCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier notFoundDb = (o) -> o
        .gateway(DbGateway.class, (mock) -> {
            when(mock.findOne(any())).thenThrow(new DbNotFoundException(Databases.IdRef));
        })
        .verify((mocks) -> {
            verify(mocks.dbGateway()).findOne(Databases.IdRef);
        });

    private SnapshotTestOptionsModifier mkRegion(Function<RegionName, CreationStatus<RegionName>> lift) {
        return (o) -> o
            .gateway(DbGateway.class, (mock) -> {
                when(mock.findOne(any())).thenReturn(Databases.One);

                when(mock.waitUntilDbStatus(any(), any(), any())).thenReturn(Duration.ofMillis(6789));
            })
            .gateway(RegionGateway.class, (mock) -> {
                doReturn(lift.apply(Regions.NAME)).when(mock).create(any(), any(), any(), any());
            })
            .verify((mocks) -> {
                verify(mocks.dbGateway()).findOne(Databases.IdRef);
                verify(mocks.regionGateway()).create(Databases.IdRef, Regions.NAME, Databases.One.getInfo().getTier(), Databases.Cloud);
            });
    }

    @TestForAllOutputs
    public void region_created(OutputType outputType) {
        verifyRun("db create-region ${DatabaseId} -r ${RegionName}", outputType, o -> o.use(mkRegion(CreationStatus::created))
            .verify((mocks) -> {
                verify(mocks.dbGateway()).waitUntilDbStatus(Databases.IdRef, ACTIVE, Duration.ofSeconds(900));
            }));
    }

    @TestForDifferentOutputs
    public void region_created_with_custom_timeout(OutputType outputType) {
        verifyRun("db create-region ${DatabaseId} -r ${RegionName} --timeout 123123", outputType, o -> o.use(mkRegion(CreationStatus::created))
            .verify((mocks) -> {
                verify(mocks.dbGateway()).waitUntilDbStatus(Databases.IdRef, ACTIVE, Duration.ofSeconds(123123));
            }));
    }

    @TestForDifferentOutputs
    public void region_create_async(OutputType outputType) {
        verifyRun("db create-region ${DatabaseId} -r ${RegionName} --async", outputType, o -> o.use(mkRegion(CreationStatus::created))
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).waitUntilDbStatus(any(), any(), any());
            }));
    }

    @TestForDifferentOutputs
    public void error_db_not_found(OutputType outputType) {
        verifyRun("db create-region ${DatabaseId} -r ${RegionName}", outputType, notFoundDb);
    }

    @TestForDifferentOutputs
    public void error_region_already_exists(OutputType outputType) {
        verifyRun("db create-region ${DatabaseId} -r ${RegionName}", outputType, o -> o.use(mkRegion(CreationStatus::alreadyExists))
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).waitUntilDbStatus(any(), any(), any());
            }));
    }

    @TestForDifferentOutputs
    public void allow_existing_region(OutputType outputType) {
        verifyRun("db create-region ${DatabaseId} -r ${RegionName} --if-not-exists --async", outputType, o -> o.use(mkRegion(CreationStatus::alreadyExists))
            .verify((mocks) -> {
                verify(mocks.dbGateway(), never()).waitUntilDbStatus(any(), any(), any());
            }));
    }
}
