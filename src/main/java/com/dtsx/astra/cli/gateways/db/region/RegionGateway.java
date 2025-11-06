package com.dtsx.astra.cli.gateways.db.region;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.db.domain.Datacenter;

import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

public interface RegionGateway extends SomeGateway {
    record RegionInfo(String displayName, boolean hasFreeTier, String zone, Object raw) {}

    SortedMap<CloudProvider, ? extends SortedMap<String, RegionInfo>> findAllServerless(boolean vector);

    SortedMap<CloudProvider, ? extends SortedMap<String, RegionInfo>> findAllClassic();

    List<Datacenter> findAllForDb(DbRef dbRef);

    SortedSet<CloudProvider> findAvailableClouds();

    CreationStatus<RegionName> create(DbRef ref, RegionName region, String tier, CloudProvider cp);

    DeletionStatus<RegionName> delete(DbRef ref, RegionName region);
}
