package com.dtsx.astra.cli.commands.db.endpoints.legacy;

import com.dtsx.astra.cli.commands.db.endpoints.AbstractEndpointGetCmd;
import com.dtsx.astra.cli.commands.db.endpoints.EndpointUtils;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import picocli.CommandLine.Command;

@Command(
    name = "get-endpoint-playground",
    description = "Get the GraphQL Playground for the specified database"
)
public class EndpointPlaygroundCmd extends AbstractEndpointGetCmd {
    @Override
    protected String mkEndpoint(EndpointGetResponse result) {
        return EndpointUtils.getPlaygroundEndpoint(result, profile().env());
    }

    @Override
    protected boolean isLegacy() {
        return true;
    }
}
