package com.dtsx.astra.cli.gateways.db.region;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

public interface RegionGateway {
    static RegionGateway mkDefault(String token, AstraEnvironment env) {
        return new RegionGatewayImpl(APIProvider.mkDefault(token, env));
    }

    record RegionInfo(String displayName, boolean hasFreeTier, String zone) {}

    SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> findServerlessRegions(boolean vector);

    SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> findClassicRegions();

    List<Datacenter> findRegionsForDb(DbRef dbRef);

    boolean regionExistsInDb(DbRef dbRef, String region);

    Set<String> findRegionClouds();

    CreationStatus<String> createRegion(DbRef ref, String region);

    DeletionStatus<String> deleteRegion(DbRef ref, String region);
}
