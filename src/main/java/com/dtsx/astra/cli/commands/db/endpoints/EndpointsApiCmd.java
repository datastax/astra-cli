package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "api",
    description = "Get the API endpoint to interact with the specified database"
)
@Example(
    comment = "Get the API endpoint for the database",
    command = "${cli.name} db endpoints api mydb"
)
@Example(
    comment = "Get the API endpoint for the database in a specific region",
    command = "${cli.name} db endpoints api mydb --region us-east1"
)
public class EndpointsApiCmd extends AbstractEndpointGetCmd {
    public EndpointsApiCmd() {
        super(Endpoint.API);
    }
}
