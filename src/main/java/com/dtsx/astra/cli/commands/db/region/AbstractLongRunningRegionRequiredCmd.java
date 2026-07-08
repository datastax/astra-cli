package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.commands.db.AbstractDbRequiredCmd;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.WithSetTimeout;
import com.dtsx.astra.cli.core.models.RegionRef;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

public abstract class AbstractLongRunningRegionRequiredCmd<OpRes> extends AbstractDbRequiredCmd<OpRes> implements WithSetTimeout {
    protected RegionRef $regionRef;

    @Option(
        names = { $Regions.LONG, $Regions.SHORT },
        description = "The region to use",
        paramLabel = $Regions.LABEL,
        required = true
    )
    private String $regionName;

    @Mixin
    protected LongRunningOptionsMixin lrMixin;

    protected RegionGateway regionGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        regionGateway = ctx.gateways().mkRegionGateway(profile().token(), profile().env());
        $regionRef = RegionRef.mustParse($regionName);
    }
}
