package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class EndpointUtils {
    public static String getApiEndpoint(EndpointGetResponse result, AstraEnvironment env) {
        val db = result.database();

        return (db.getInfo().getDbType() == null)
            ? ApiLocator.getApiRestEndpoint(env, db.getId(), result.region())
            : ApiLocator.getApiEndpoint(env, db.getId(), result.region());
    }
    
    public static String getSwaggerEndpoint(EndpointGetResponse result, AstraEnvironment env) {
        val db = result.database();

        return (db.getInfo().getDbType() == null)
            ? ApiLocator.getApiRestEndpoint(env, db.getId(), result.region()) + "/swagger-ui/"
            : ApiLocator.getApiEndpoint(env, db.getId(), result.region()) + "/api/json/swagger-ui/";
    }
    
    public static String getPlaygroundEndpoint(EndpointGetResponse result, AstraEnvironment env) {
        return ApiLocator.getApiGraphQLEndPoint(env, result.database().getId(), result.region()) + "/playground";
    }
}
