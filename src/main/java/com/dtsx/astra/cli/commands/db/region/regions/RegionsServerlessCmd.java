package com.dtsx.astra.cli.commands.db.region.regions;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.FoundRegion;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.RegionListRequest;
import com.dtsx.astra.cli.operations.db.region.RegionListServerlessOperation;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.stream.Stream;

@Command(
    name = "serverless",
    description = "List all available regions for serverless Astra DB databases"
)
@Example(
    comment = "List all available regions for serverless Astra DB databases",
    command = "${cli.name} db regions serverless"
)
@Example(
    comment = "Filter by cloud provider",
    command = "${cli.name} db regions serverless --cloud AWS,GCP"
)
@Example(
    comment = "Filter by partial region name",
    command = "${cli.name} db regions serverless --filter us-,ca-"
)
@Example(
    comment = "Filter by zone",
    command = "${cli.name} db regions serverless --zone Europe"
)
public class RegionsServerlessCmd extends AbstractRegionListCmd {
    @Option(
        names = { "-a", "--all" },
        description = "List all available regions, including those not enabled for the current organization",
        defaultValue = "false"
    )
    public boolean $all;

    @Override
    protected Operation<Stream<FoundRegion>> mkOperation() {
        return new RegionListServerlessOperation(regionGateway, new RegionListRequest(
            $nameFilter,
            $cloudFilter,
            $zoneFilter,
            $all
        ));
    }
}
