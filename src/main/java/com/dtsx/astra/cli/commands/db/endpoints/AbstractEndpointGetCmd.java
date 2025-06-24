package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.commands.db.AbstractDbSpecificCmd;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import picocli.CommandLine.Option;

import java.util.Optional;

public abstract class AbstractEndpointGetCmd extends AbstractDbSpecificCmd<String> {
    @Option(
        names = { "--region", "-r" },
        description = { "The region to use" },
        paramLabel = "REGION"
    )
    protected Optional<RegionName> region;

    @Override
    protected final OutputAll execute(String result) {
        return OutputAll.serializeValue(result);
    }
}
