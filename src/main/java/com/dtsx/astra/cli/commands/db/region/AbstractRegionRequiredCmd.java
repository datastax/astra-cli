package com.dtsx.astra.cli.commands.db.region;

import picocli.CommandLine.Option;

public abstract class AbstractRegionRequiredCmd extends AbstractDbSpecificRegionCmd {
    @Option(
        names = { "--region", "-r" },
        description = { "The region to use" },
        paramLabel = "REGION",
        required = true
    )
    protected String region;
}
