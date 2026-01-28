package com.dtsx.astra.cli.commands.db.endpoints.legacy;

import com.dtsx.astra.cli.commands.db.endpoints.Endpoint;
import picocli.CommandLine.Command;

@Command(
    name = "get-endpoint-api",
    description = "Get the API endpoint to interact with the specified database"
)
public class EndpointApiCmd extends AbstractLegacyEndpointGetCmd {
    public EndpointApiCmd() {
        super(Endpoint.API);
    }
}
