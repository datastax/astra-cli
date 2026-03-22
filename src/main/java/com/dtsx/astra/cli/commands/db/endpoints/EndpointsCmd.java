package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.mixins.HelpMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "endpoints",
    description = "Get various endpoints for your database",
    subcommands = {
        EndpointsListCmd.class,
        EndpointsApiCmd.class,
        EndpointsDataApiCmd.class,
        EndpointsSwaggerCmd.class,
        EndpointsPlaygroundCmd.class,
    }
)
public class EndpointsCmd {
    @Mixin
    public HelpMixin help;
}
