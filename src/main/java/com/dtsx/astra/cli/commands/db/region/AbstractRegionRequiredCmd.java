package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.models.RegionName;
import picocli.CommandLine.Option;

public abstract class AbstractRegionRequiredCmd<OpRes> extends AbstractDbSpecificRegionCmd<OpRes> {
    @Option(
        names = { "--region", "-r" },
        description = { "The region to use" },
        paramLabel = "REGION",
        required = true
    )
    protected RegionName region;
}
