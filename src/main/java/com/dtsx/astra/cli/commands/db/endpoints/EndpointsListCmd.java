package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.commands.db.AbstractPromptForDbCmd;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetRequest;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.Supplier;

@Command(
    name = "list",
    description = "Get the various endpoints for a database"
)
@Example(
    comment = "List the various endpoints for the specified database",
    command = "${cli.name} db endpoints list my_db"
)
@Example(
    comment = "List the various endpoints for a prompted db",
    command = "${cli.name} db endpoints list"
)
public class EndpointsListCmd extends AbstractPromptForDbCmd<EndpointGetResponse> {
    @Option(
        names = { $Regions.LONG, $Regions.SHORT },
        description = "The region to use",
        paramLabel = $Regions.LABEL
    )
    protected Optional<RegionName> region;

    @Override
    protected OutputAll execute(Supplier<EndpointGetResponse> result) {
        val endpoints = new LinkedHashMap<String, Object>();

        for (val endpoint : Endpoint.values()) {
            endpoints.put(endpoint.displayName(), endpoint.mkUrl(result.get(), profile().env()));
        }

        return ShellTable.forAttributes(endpoints, "Endpoint", "URL");
    }

    @Override
    protected final Operation<EndpointGetResponse> mkOperation() {
        return new EndpointGetOperation(dbGateway, new EndpointGetRequest($dbRef, region, profile().env()));
    }

    @Override
    protected final String dbRefPrompt() {
        return "Select the database to get the endpoint for";
    }
}
