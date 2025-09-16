package com.dtsx.astra.cli.snapshot.commands.db.region;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;

import static org.mockito.Mockito.*;

public class RegionListClassicCmdSnapshotTest extends BaseRegionListCmdSnapshotTest {
    @Override
    protected String getCommandName() {
        return "list-regions-classic";
    }

    @Override
    protected void verifyMockGateway(RegionGateway mock) {
        verify(mock).findAllClassic();
    }
}
