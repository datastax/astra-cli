package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import com.dtsx.astra.sdk.utils.ApiLocator;
import picocli.CommandLine.Command;

@Command(
    name = "get-endpoint-playground"
)
public class EndpointPlaygroundCmd extends AbstractEndpointGetCmd {
    @Override
    protected String mkEndpoint(EndpointGetResponse result) {
        return ApiLocator.getApiGraphQLEndPoint(profile().env(), result.database().getId(), result.region()) + "/playground";
    }
}
