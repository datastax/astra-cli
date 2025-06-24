package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import picocli.CommandLine.Command;

@Command(
    name = "get-endpoint-swagger"
)
public class EndpointSwaggerCmd extends AbstractEndpointGetCmd {
    @Override
    protected String extractEndpoint(Database db, RegionName region, AstraEnvironment env) {
        return (db.getInfo().getDbType() == null)
            ? ApiLocator.getApiRestEndpoint(env, db.getId(), region.unwrap()) + "/swagger-ui/"
            : ApiLocator.getApiEndpoint(env, db.getId(), region.unwrap()) + "/api/json/swagger-ui/";
    }
}
