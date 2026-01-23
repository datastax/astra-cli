package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.mixins.HelpMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "get-endpoint",
    description = "Get various endpoints for your database",
    subcommands = {
        EndpointApiCmd.class,
        EndpointDataApiCmd.class,
        EndpointSwaggerCmd.class,
        EndpointPlaygroundCmd.class
    }
)
public class GetEndpointCmd {
    @Mixin
    public HelpMixin help;
}
