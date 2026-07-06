package com.dtsx.astra.cli.gateways.db.region;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionRef;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.db.domain.Datacenter;

import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;

public interface RegionGateway extends SomeGateway {
    record RegionInfo(String displayName, boolean hasFreeTier, String zone, Object raw) {}

    SortedMap<CloudProvider, ? extends SortedMap<String, RegionInfo>> findAllServerless(boolean vector, boolean all);

    SortedMap<CloudProvider, ? extends SortedMap<String, RegionInfo>> findAllClassic();

    List<Datacenter> findAllForDb(DbRef dbRef);

    SortedSet<CloudProvider> findAvailableClouds();

    CreationStatus<RegionRef> create(DbRef ref, RegionRef region, String tier, CloudProvider cp);

    DeletionStatus<RegionRef> delete(DbRef ref, RegionRef region);

    CloudProvider findCloudForRegion(Optional<CloudProvider> cloud, RegionRef region, boolean vectorOnly);
}
