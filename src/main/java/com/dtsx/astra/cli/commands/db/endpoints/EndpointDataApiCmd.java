package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import picocli.CommandLine.Command;

@Command(
    name = "data-api",
    description = "Get the Data API endpoint for the specified database"
)
@Example(
    comment = "Get the Data API endpoint for the database",
    command = "${cli.name} db get-endpoint data-api mydb"
)
@Example(
    comment = "Get the Data API endpoint for the database in a specific region",
    command = "${cli.name} db get-endpoint data-api mydb --region us-east1"
)
public class EndpointDataApiCmd extends AbstractEndpointGetCmd {
    @Override
    protected String mkEndpoint(EndpointGetResponse result) {
        return EndpointUtils.getApiEndpoint(result, profile().env()) + "/api/v1/json";
    }
}
