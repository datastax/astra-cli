package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.models.RegionName;
import picocli.CommandLine.Option;

public abstract class AbstractRegionRequiredCmd<OpRes> extends AbstractDbRequiredRegionCmd<OpRes> {
    @Option(
        names = { $Regions.LONG, $Regions.SHORT },
        description = "The region to use",
        paramLabel = $Regions.LABEL,
        required = true
    )
    protected RegionName $region;
}
