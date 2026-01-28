package com.dtsx.astra.cli.snapshot.commands.db.region.regions;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;

import static org.mockito.Mockito.verify;

public class RegionsClassicCmdSnapshotTest extends BaseRegionsCmdSnapshotTest {
    protected RegionsClassicCmdSnapshotTest() {
        super("regions", "classic");
    }

    @Override
    protected void verifyMockGateway(RegionGateway mock) {
        verify(mock).findAllClassic();
    }
}
