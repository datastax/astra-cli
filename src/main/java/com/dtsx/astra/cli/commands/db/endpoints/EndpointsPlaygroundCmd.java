package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "playground",
    description = "Get the GraphQL Playground for the specified database"
)
@Example(
    comment = "Get the GraphQL Playground for the database",
    command = "${cli.name} db endpoints playground mydb"
)
@Example(
    comment = "Get the GraphQL Playground for the database in a specific region",
    command = "${cli.name} db endpoints playground mydb --region us-east1"
)
public class EndpointsPlaygroundCmd extends AbstractEndpointGetCmd {
    public EndpointsPlaygroundCmd() {
        super(Endpoint.PLAYGROUND);
    }
}
