package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.commands.db.keyspace.AbstractKeyspaceCmd;
import picocli.CommandLine.Option;

public abstract class AbstractRegionRequiredCmd extends AbstractKeyspaceCmd {
    @Option(
        names = { "--region", "-r" },
        description = { "The region to use" },
        paramLabel = "REGION",
        required = true
    )
    protected String region;
}
