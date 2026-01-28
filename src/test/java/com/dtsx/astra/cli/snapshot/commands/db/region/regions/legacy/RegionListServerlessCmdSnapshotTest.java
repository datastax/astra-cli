package com.dtsx.astra.cli.snapshot.commands.db.region.regions.legacy;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;

import static org.mockito.Mockito.verify;

public class RegionListServerlessCmdSnapshotTest extends BaseRegionListCmdSnapshotTest {
    protected RegionListServerlessCmdSnapshotTest() {
        super("list-regions-serverless");
    }

    @Override
    protected void verifyMockGateway(RegionGateway mock) {
        verify(mock).findAllServerless(false);
    }
}
