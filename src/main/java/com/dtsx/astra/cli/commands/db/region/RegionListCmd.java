package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.region.RegionListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;

import static com.dtsx.astra.cli.operations.db.region.RegionListOperation.*;

@Command(
    name = "list-regions"
)
public class RegionListCmd extends AbstractDbSpecificRegionCmd<FoundRegions> {
    @Override
    protected RegionListOperation mkOperation() {
        return new RegionListOperation(regionGateway, dbGateway, new RegionListRequest(dbRef));
    }

    @Override
    protected final OutputAll execute(FoundRegions result) {
        val data = result.regions().stream()
            .map((dc) -> Map.of(
                "Cloud Provider", dc.getCloudProvider().name().toLowerCase(),
                "Region", dc.getRegion() + (dc == result.defaultRegion() ? ShellTable.highlight(" (default)") : ""),
                "Tier", dc.getTier(),
                "Status", dc.getStatus()
            ))
            .toList();

        return new ShellTable(data).withColumns("Cloud Provider", "Region", "Tier", "Status");
    }
}
