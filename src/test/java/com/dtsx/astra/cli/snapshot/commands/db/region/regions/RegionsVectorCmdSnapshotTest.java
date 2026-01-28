package com.dtsx.astra.cli.snapshot.commands.db.region.regions;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;

import static org.mockito.Mockito.verify;

public class RegionsVectorCmdSnapshotTest extends BaseRegionsCmdSnapshotTest {
    protected RegionsVectorCmdSnapshotTest() {
        super("regions", "vector");
    }

    @Override
    protected void verifyMockGateway(RegionGateway mock) {
        verify(mock).findAllServerless(true);
    }
}
