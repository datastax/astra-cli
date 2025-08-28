package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.region.RegionListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.db.region.RegionListOperation.*;

@Command(
    name = "list-regions",
    description = "List the regions for the given database"
)
@Example(
    comment = "List all regions for the database",
    command = "${cli.name} db list-regions my_db"
)
public class RegionListCmd extends AbstractPromptForDbRegionCmd<FoundRegions> {
    @Override
    protected RegionListOperation mkOperation() {
        return new RegionListOperation(ctx, regionGateway, dbGateway, new RegionListRequest($dbRef));
    }

    @Override
    protected final OutputJson executeJson(Supplier<FoundRegions> result) {
        return OutputJson.serializeValue(result.get().regions());
    }

    @Override
    protected final OutputAll execute(Supplier<FoundRegions> result) {
        val data = result.get().regions().stream()
            .map((dc) -> Map.of(
                "Cloud Provider", dc.getCloudProvider().name().toLowerCase(),
                "Region", dc.getRegion() + (dc == result.get().defaultRegion() ? ShellTable.highlight(ctx, " (default)") : ""),
                "Tier", dc.getTier(),
                "Status", dc.getStatus()
            ))
            .toList();

        return new ShellTable(data).withColumns("Cloud Provider", "Region", "Tier", "Status");
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to list regions for";
    }
}
