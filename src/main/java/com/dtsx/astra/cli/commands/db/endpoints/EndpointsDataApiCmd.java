package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "curl",
    aliases = {"data-api"},
    description = "Get the Data API curl endpoint for the specified database"
)
@Example(
    comment = "Get the Data API curl endpoint for the database",
    command = "${cli.name} db endpoints curl mydb"
)
@Example(
    comment = "Get the Data API curl endpoint for the database in a specific region",
    command = "${cli.name} db endpoints curl mydb --region us-east1"
)
public class EndpointsDataApiCmd extends AbstractEndpointGetCmd {
    public EndpointsDataApiCmd() {
        super(Endpoint.CURL);
    }
}
