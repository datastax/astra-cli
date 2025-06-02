package com.dtsx.astra.cli.services;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;

public interface APIProvider {
    static APIProvider mkDefault(String profileName, String token, AstraEnvironment env) {
        return new Default(profileName, token, env);
    }

    AstraOpsClient devopsApiClient();
    DataAPIClient dataApiClient();

    @RequiredArgsConstructor
    class Default implements APIProvider {
        private final String profileName;
        private final String token;
        private final AstraEnvironment env;

        private @Nullable AstraOpsClient cachedDevopsApiClient;
        private @Nullable DataAPIClient cachedDataApiClient;

        @Override
        public AstraOpsClient devopsApiClient() {
            if (cachedDevopsApiClient == null) {
                cachedDevopsApiClient = new AstraOpsClient(token, env);
            }
            return cachedDevopsApiClient;
        }

        @Override
        public DataAPIClient dataApiClient() {
            if (cachedDataApiClient == null) {
                val destination = switch (env) {
                    case PROD -> DataAPIDestination.ASTRA;
                    case DEV -> DataAPIDestination.ASTRA_DEV;
                    case TEST -> DataAPIDestination.ASTRA_TEST;
                };

                cachedDataApiClient = new DataAPIClient(token, new DataAPIClientOptions().destination(destination));
            }
            return cachedDataApiClient;
        }
    }
}
