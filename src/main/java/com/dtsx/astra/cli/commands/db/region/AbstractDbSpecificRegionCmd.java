package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.commands.db.AbstractDbSpecificCmd;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractDbSpecificRegionCmd extends AbstractDbSpecificCmd {
    protected RegionGateway regionGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        regionGateway = RegionGateway.mkDefault(profile().token(), profile().env());
    }
}
