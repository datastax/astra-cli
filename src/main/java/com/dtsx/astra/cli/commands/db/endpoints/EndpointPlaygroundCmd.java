package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import picocli.CommandLine.Command;

@Command(
    name = "get-endpoint-playground"
)
public class EndpointPlaygroundCmd extends AbstractEndpointGetCmd {
    @Override
    protected String extractEndpoint(Database db, String region, AstraEnvironment env) {
        return ApiLocator.getApiGraphQLEndPoint(env, db.getId(), region) + "/playground";
    }
}
