package com.dtsx.astra.cli.gateways.db.region;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.models.Token;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

public interface RegionGateway {
    static RegionGateway mkDefault(Token token, AstraEnvironment env) {
        return new RegionGatewayImpl(APIProvider.mkDefault(token, env));
    }

    record RegionInfo(String displayName, boolean hasFreeTier, String zone, Object raw) {}

    SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> findServerlessRegions(boolean vector);

    SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> findClassicRegions();

    List<Datacenter> findRegionsForDb(DbRef dbRef);

    boolean regionExistsInDb(DbRef dbRef, RegionName region);

    Set<String> findRegionClouds();

    CreationStatus<RegionName> createRegion(DbRef ref, RegionName region, String tier, CloudProviderType cp);

    DeletionStatus<RegionName> deleteRegion(DbRef ref, RegionName region);
}
