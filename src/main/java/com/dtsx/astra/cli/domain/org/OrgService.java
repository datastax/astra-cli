package com.dtsx.astra.cli.domain.org;

import com.dtsx.astra.cli.domain.APIProvider;
import com.dtsx.astra.sdk.db.domain.RegionType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.SortedMap;
import java.util.TreeMap;

public interface OrgService {
    static OrgService mkDefault(String token, AstraEnvironment env) {
        return new OrgServiceImpl(APIProvider.mkDefault(token, env));
    }

    SortedMap<String, TreeMap<String, String>> getDbServerlessRegions(RegionType regionType);
}
