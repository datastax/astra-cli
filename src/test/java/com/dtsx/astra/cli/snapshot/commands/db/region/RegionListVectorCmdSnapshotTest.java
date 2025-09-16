package com.dtsx.astra.cli.snapshot.commands.db.region;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;

import static org.mockito.Mockito.*;

public class RegionListVectorCmdSnapshotTest extends BaseRegionListCmdSnapshotTest {
    @Override
    protected String getCommandName() {
        return "list-regions-vector";
    }

    @Override
    protected void verifyMockGateway(RegionGateway mock) {
        verify(mock).findAllServerless(true);
    }
}
