package com.dtsx.astra.cli.gateways.streaming;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.streaming.domain.Tenant;
import org.graalvm.collections.Pair;

import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.stream.Stream;

public interface StreamingGateway extends SomeGateway {
    Tenant findOne(TenantName tenantName);

    Stream<Tenant> findAll();

    boolean exists(TenantName tenantName);

    CloudProvider findCloudForRegion(Optional<CloudProvider> cloud, RegionName region);

    CreationStatus<Tenant> create(TenantName tenantName, Either<String, Pair<CloudProvider, RegionName>> clusterOrCloud, String plan, String userEmail);

    DeletionStatus<TenantName> delete(TenantName tenantName);

    record StreamingRegionInfo(String displayName, boolean isPremium, Object raw) {}

    SortedMap<CloudProvider, ? extends SortedMap<String, StreamingRegionInfo>> findAllRegions();

    SortedSet<CloudProvider> findAvailableClouds();
}
