package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "endpoints",
    description = "Get the various endpoints for your database",
    subcommands = {
        EndpointsListCmd.class,
        EndpointsApiCmd.class,
        EndpointsDataApiCmd.class,
        EndpointsSwaggerCmd.class,
        EndpointsPlaygroundCmd.class,
    }
)
@Example(
    comment = "List the various endpoints for the specified database",
    command = "${cli.name} db endpoints my_db"
)
@Example(
    comment = "Get a specific endpoint for the specified database",
    command = "${cli.name} db endpoints data-api-client my_db"
)
@AliasForSubcommand(EndpointsListCmd.class)
public class EndpointsCmd extends EndpointsListImpl {}
