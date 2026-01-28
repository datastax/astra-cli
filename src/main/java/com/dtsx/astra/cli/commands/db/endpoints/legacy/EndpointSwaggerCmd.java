package com.dtsx.astra.cli.commands.db.endpoints.legacy;

import com.dtsx.astra.cli.commands.db.endpoints.Endpoint;
import picocli.CommandLine.Command;

@Command(
    name = "get-endpoint-swagger",
    description = "Get the SwaggerUI endpoint for the specified database"
)
public class EndpointSwaggerCmd extends AbstractLegacyEndpointGetCmd {
    public EndpointSwaggerCmd() {
        super(Endpoint.SWAGGER);
    }
}
