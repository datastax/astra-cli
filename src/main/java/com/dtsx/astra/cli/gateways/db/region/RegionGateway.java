package com.dtsx.astra.cli.gateways.db.region;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

public interface RegionGateway {
    static RegionGateway mkDefault(String token, AstraEnvironment env) {
        return new RegionGatewayImpl(APIProvider.mkDefault(token, env));
    }

    void deleteRegion(DbRef ref, String region);
}
