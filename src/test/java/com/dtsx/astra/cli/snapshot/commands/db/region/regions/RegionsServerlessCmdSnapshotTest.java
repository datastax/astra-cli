package com.dtsx.astra.cli.snapshot.commands.db.region.regions;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;

import static org.mockito.Mockito.verify;

public class RegionsServerlessCmdSnapshotTest extends BaseRegionsCmdSnapshotTest {
    protected RegionsServerlessCmdSnapshotTest() {
        super("regions", "serverless");
    }

    @Override
    protected void verifyMockGateway(RegionGateway mock) {
        verify(mock).findAllServerless(false);
    }
}
