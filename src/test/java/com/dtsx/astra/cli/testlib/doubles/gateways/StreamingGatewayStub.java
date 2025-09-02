package com.dtsx.astra.cli.testlib.doubles.gateways;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.streaming.domain.Tenant;
import org.graalvm.collections.Pair;

import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.testlib.doubles.Utils.methodIllegallyCalled;

public class StreamingGatewayStub extends GatewayStub implements StreamingGateway {
    @Override
    public Tenant findOne(TenantName tenantName) {
        return methodIllegallyCalled();
    }

    @Override
    public Stream<Tenant> findAll() {
        return methodIllegallyCalled();
    }

    @Override
    public boolean exists(TenantName tenantName) {
        return methodIllegallyCalled();
    }

    @Override
    public CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, RegionName region) {
        return methodIllegallyCalled();
    }

    @Override
    public CreationStatus<Tenant> create(TenantName tenantName, Either<String, Pair<CloudProviderType, RegionName>> clusterOrCloud, String plan, String userEmail) {
        return methodIllegallyCalled();
    }

    @Override
    public DeletionStatus<TenantName> delete(TenantName tenantName) {
        return methodIllegallyCalled();
    }

    @Override
    public SortedMap<CloudProviderType, ? extends SortedMap<String, StreamingRegionInfo>> findAllRegions() {
        return methodIllegallyCalled();
    }

    @Override
    public Set<CloudProviderType> findAvailableClouds() {
        return methodIllegallyCalled();
    }
}
