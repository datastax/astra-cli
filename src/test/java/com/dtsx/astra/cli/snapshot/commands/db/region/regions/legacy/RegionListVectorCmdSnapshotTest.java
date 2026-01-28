package com.dtsx.astra.cli.snapshot.commands.db.region.regions.legacy;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;

import static org.mockito.Mockito.verify;

public class RegionListVectorCmdSnapshotTest extends BaseRegionListCmdSnapshotTest {
    protected RegionListVectorCmdSnapshotTest() {
        super("list-regions-vector");
    }

    @Override
    protected void verifyMockGateway(RegionGateway mock) {
        verify(mock).findAllServerless(true);
    }
}
