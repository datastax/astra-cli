package com.dtsx.astra.cli.snapshot.commands.db.region;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;

import static org.mockito.Mockito.*;

public class RegionListServerlessCmdSnapshotTest extends BaseRegionListCmdSnapshotTest {
    @Override
    protected String getCommandName() {
        return "list-regions-serverless";
    }

    @Override
    protected void verifyMockGateway(RegionGateway mock) {
        verify(mock).findAllServerless(false);
    }
}
