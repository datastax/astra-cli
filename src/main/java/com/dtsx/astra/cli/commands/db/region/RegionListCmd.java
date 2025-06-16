package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.region.RegionListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;

@Command(
    name = "list-regions"
)
public final class RegionListCmd extends AbstractDbSpecificRegionCmd {
    @Override
    protected OutputAll execute() {
        val result = new RegionListOperation(regionGateway, dbGateway).execute(dbRef);

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
