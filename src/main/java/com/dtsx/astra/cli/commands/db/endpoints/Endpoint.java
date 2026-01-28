package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.function.BiFunction;

@RequiredArgsConstructor
public enum Endpoint {
    API("api", (result, env) -> {
        val db = result.database();

        return (db.getInfo().getDbType() == null)
            ? ApiLocator.getApiRestEndpoint(env, db.getId(), result.region())
            : ApiLocator.getApiEndpoint(env, db.getId(), result.region());
    }),

    SWAGGER("swagger", (result, env) -> {
        val db = result.database();

        return (db.getInfo().getDbType() == null)
            ? ApiLocator.getApiRestEndpoint(env, db.getId(), result.region()) + "/swagger-ui"
            : ApiLocator.getApiEndpoint(env, db.getId(), result.region()) + "/api/json/swagger-ui";
    }),

    PLAYGROUND("playground", (result, env) -> {
        return ApiLocator.getApiGraphQLEndPoint(env, result.database().getId(), result.region()) + "/playground";
    }),

    DATA_API("data-api", (result, env) -> {
        return API.mkUrl.apply(result, env) + "/api/v1/json";
    });

    @Getter
    private final String displayName;
    private final BiFunction<EndpointGetResponse, AstraEnvironment, String> mkUrl;

    public String mkUrl(EndpointGetResponse result, AstraEnvironment env) {
        return mkUrl.apply(result, env);
    }
}
