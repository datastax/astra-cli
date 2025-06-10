package com.dtsx.astra.cli.gateways.org;

import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.db.domain.RegionType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.SortedMap;
import java.util.TreeMap;

public interface OrgGateway {
    static OrgGateway mkDefault(String token, AstraEnvironment env) {
        return new OrgGatewayImpl(APIProvider.mkDefault(token, env));
    }

    SortedMap<String, TreeMap<String, String>> getDbServerlessRegions(RegionType regionType);
}
