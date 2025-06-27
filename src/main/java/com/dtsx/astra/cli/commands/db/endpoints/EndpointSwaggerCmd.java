package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import com.dtsx.astra.sdk.utils.ApiLocator;
import lombok.val;
import picocli.CommandLine.Command;

@Command(
    name = "get-endpoint-swagger",
    description = "Get the SwaggerUI endpoint for the specified database"
)
@Example(
    comment = "Get the SwaggerUI endpoint for the database",
    command = "astra db get-endpoint-swagger mydb"
)
@Example(
    comment = "Get the SwaggerUI endpoint for the database in a specific region",
    command = "astra db get-endpoint-swagger mydb --region us-east1"
)
public class EndpointSwaggerCmd extends AbstractEndpointGetCmd {
    @Override
    protected String mkEndpoint(EndpointGetResponse result) {
        val db = result.database();

        return (db.getInfo().getDbType() == null)
            ? ApiLocator.getApiRestEndpoint(profile().env(), db.getId(), result.region()) + "/swagger-ui/"
            : ApiLocator.getApiEndpoint(profile().env(), db.getId(), result.region()) + "/api/json/swagger-ui/";
    }
}
