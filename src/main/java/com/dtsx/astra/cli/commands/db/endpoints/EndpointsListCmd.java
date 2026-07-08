package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "list",
    description = "Get the various endpoints for your database"
)
@Example(
    comment = "List the various endpoints for the specified database",
    command = "${cli.name} db endpoints list my_db"
)
@Example(
    comment = "List the various endpoints for a prompted db",
    command = "${cli.name} db endpoints list"
)
public class EndpointsListCmd extends EndpointsListImpl {}
