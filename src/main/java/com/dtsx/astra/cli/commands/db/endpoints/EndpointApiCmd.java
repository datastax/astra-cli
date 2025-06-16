package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import picocli.CommandLine.Command;

@Command(
    name = "get-endpoint-api"
)
public class EndpointApiCmd extends AbstractEndpointGetCmd {
    @Override
    protected String extractEndpoint(Database db, String region, AstraEnvironment env) {
        return (db.getInfo().getDbType() == null)
            ? ApiLocator.getApiRestEndpoint(env, db.getId(), region)
            : ApiLocator.getApiEndpoint(env, db.getId(), region);
    }
}
