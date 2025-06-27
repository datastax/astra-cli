package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import com.dtsx.astra.sdk.utils.ApiLocator;
import picocli.CommandLine.Command;

@Command(
    name = "get-endpoint-playground",
    description = "Get the GraphQL Playground for the specified database"
)
@Example(
    comment = "Get the GraphQL Playground for the database",
    command = "astra db get-endpoint-playground mydb"
)
@Example(
    comment = "Get the GraphQL Playground for the database in a specific region",
    command = "astra db get-endpoint-playground mydb --region us-east1"
)
public class EndpointPlaygroundCmd extends AbstractEndpointGetCmd {
    @Override
    protected String mkEndpoint(EndpointGetResponse result) {
        return ApiLocator.getApiGraphQLEndPoint(profile().env(), result.database().getId(), result.region()) + "/playground";
    }
}
