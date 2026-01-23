package com.dtsx.astra.cli.commands.db.endpoints.legacy;

import com.dtsx.astra.cli.commands.db.endpoints.EndpointUtils;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import picocli.CommandLine.Command;

@Command(
    name = "get-endpoint-api",
    description = "Get the API endpoint to interact with the specified database"
)
public class EndpointApiCmd extends AbstractLegacyEndpointGetCmd {
    @Override
    protected String mkEndpoint(EndpointGetResponse result) {
        return EndpointUtils.getApiEndpoint(result, profile().env());
    }
}
