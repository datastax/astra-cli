package com.dtsx.astra.cli.commands.db.endpoints.legacy;

import com.dtsx.astra.cli.commands.db.endpoints.Endpoint;
import picocli.CommandLine.Command;

@Command(
    name = "get-endpoint-playground",
    description = "Get the GraphQL Playground for the specified database"
)
public class EndpointPlaygroundCmd extends AbstractLegacyEndpointGetCmd {
    public EndpointPlaygroundCmd() {
        super(Endpoint.PLAYGROUND);
    }
}
