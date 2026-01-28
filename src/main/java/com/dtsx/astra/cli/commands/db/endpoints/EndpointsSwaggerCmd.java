package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "swagger",
    description = "Get the SwaggerUI endpoint for the specified database"
)
@Example(
    comment = "Get the SwaggerUI endpoint for the database",
    command = "${cli.name} db endpoints swagger mydb"
)
@Example(
    comment = "Get the SwaggerUI endpoint for the database in a specific region",
    command = "${cli.name} db endpoints swagger mydb --region us-east1"
)
public class EndpointsSwaggerCmd extends AbstractEndpointGetCmd {
    public EndpointsSwaggerCmd() {
        super(Endpoint.SWAGGER);
    }
}
