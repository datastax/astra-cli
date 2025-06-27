package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import com.dtsx.astra.sdk.utils.ApiLocator;
import lombok.val;
import picocli.CommandLine.Command;

@Command(
    name = "get-endpoint-api",
    description = "Get the API endpoint to interact with the specified database"
)
@Example(
    comment = "Get the API endpoint for the database",
    command = "astra db get-endpoint-api mydb"
)
@Example(
    comment = "Get the API endpoint for the database in a specific region",
    command = "astra db get-endpoint-api mydb --region us-east1"
)
public class EndpointApiCmd extends AbstractEndpointGetCmd {
    @Override
    protected String mkEndpoint(EndpointGetResponse result) {
        val db = result.database();

        return (db.getInfo().getDbType() == null)
            ? ApiLocator.getApiRestEndpoint(profile().env(), db.getId(), result.region())
            : ApiLocator.getApiEndpoint(profile().env(), db.getId(), result.region());
    }
}
