package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "client",
    description = "Get the Data API client endpoint for the specified database"
)
@Example(
    comment = "Get the Data API client endpoint for the database",
    command = "${cli.name} db endpoints client mydb"
)
@Example(
    comment = "Get the Data API client endpoint for the database in a specific region",
    command = "${cli.name} db endpoints client mydb --region us-east1"
)
public class EndpointsClientCmd extends AbstractEndpointGetCmd {
    public EndpointsClientCmd() {
        super(Endpoint.CLIENT);
    }
}
