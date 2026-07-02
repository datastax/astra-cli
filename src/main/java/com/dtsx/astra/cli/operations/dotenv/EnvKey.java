package com.dtsx.astra.cli.operations.dotenv;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EnvKey {
    ASTRA_ORG_ID(false),
    ASTRA_ORG_NAME(false),
    ASTRA_ORG_TOKEN(false),

    ASTRA_DB_ID(true),
    ASTRA_DB_NAME(true),
    ASTRA_DB_REGION(true),
    ASTRA_DB_KEYSPACE(true),
    ASTRA_DB_APPLICATION_TOKEN(false),
    ASTRA_DB_ENVIRONMENT(true),

    ASTRA_DB_SECURE_BUNDLE_PATH(true),
    ASTRA_DB_SECURE_BUNDLE_URL(true),

    ASTRA_DB_GRAPHQL_URL(true),
    ASTRA_DB_GRAPHQL_URL_PLAYGROUND(true),
    ASTRA_DB_GRAPHQL_URL_SCHEMA(true),
    ASTRA_DB_GRAPHQL_URL_ADMIN(true),

    ASTRA_DB_API_ENDPOINT(true),
    ASTRA_DB_API_ENDPOINT_SWAGGER(true),

    ASTRA_DB_REST_URL(true),
    ASTRA_DB_REST_URL_SWAGGER(true);

    public boolean needsDb() {
        return this.needsDb;
    }

    private final boolean needsDb;
}
