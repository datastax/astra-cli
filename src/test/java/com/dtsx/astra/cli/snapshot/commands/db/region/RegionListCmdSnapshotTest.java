package com.dtsx.astra.cli.snapshot.commands.db.region;

import com.dtsx.astra.cli.core.exceptions.internal.db.DbNotFoundException;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;
import com.dtsx.astra.cli.testlib.Fixtures.Regions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RegionListCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier foundDb = (o) -> o
        .gateway(DbGateway.class, (mock) -> {
            when(mock.findOne(any())).thenReturn(Databases.One);
        })
        .gateway(RegionGateway.class, (mock) -> {
            when(mock.findAllForDb(any())).thenReturn(Regions.DATACENTERS);
        })
        .verify((mocks) -> {
            verify(mocks.dbGateway()).findOne(Databases.NameRef);
            verify(mocks.regionGateway()).findAllForDb(Databases.NameRef);
        });

    private final SnapshotTestOptionsModifier notFoundDb = (o) -> o
        .gateway(RegionGateway.class, (mock) -> {
            when(mock.findAllForDb(any())).thenThrow(new DbNotFoundException(Databases.NameRef));
        })
        .verify((mocks) -> {
            verify(mocks.regionGateway()).findAllForDb(Databases.NameRef);
            verify(mocks.dbGateway(), never()).findOne(any());
        });

    @TestForAllOutputs
    public void regions_found(OutputType outputType) {
        verifyRun("db list-regions ${DatabaseName}", outputType, foundDb);
    }

    @TestForDifferentOutputs
    public void error_db_not_found(OutputType outputType) {
        verifyRun("db list-regions ${DatabaseName}", outputType, notFoundDb);
    }
}
