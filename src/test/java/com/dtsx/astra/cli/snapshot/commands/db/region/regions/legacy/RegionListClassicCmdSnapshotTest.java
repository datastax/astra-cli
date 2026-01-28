package com.dtsx.astra.cli.snapshot.commands.db.region.regions.legacy;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;

import static org.mockito.Mockito.verify;

public class RegionListClassicCmdSnapshotTest extends BaseRegionListCmdSnapshotTest {
    protected RegionListClassicCmdSnapshotTest() {
        super("list-regions-classic");
    }

    @Override
    protected void verifyMockGateway(RegionGateway mock) {
        verify(mock).findAllClassic();
    }
}
