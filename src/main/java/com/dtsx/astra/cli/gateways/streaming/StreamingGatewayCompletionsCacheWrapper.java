package com.dtsx.astra.cli.gateways.streaming;

import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.streaming.domain.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.graalvm.collections.Pair;

import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class StreamingGatewayCompletionsCacheWrapper implements StreamingGateway {
    private final StreamingGateway delegate;
    private final CompletionsCache cache;

    @Override
    public Stream<Tenant> findAll() {
        val tenants = delegate.findAll().toList();
        cache.setCache(tenants.stream().map(Tenant::getTenantName).toList());
        return tenants.stream();
    }

    @Override
    public boolean exists(TenantName tenantName) {
        val exists = delegate.exists(tenantName);
        if (exists) {
            cache.addToCache(tenantName.unwrap());
        } else {
            cache.removeFromCache(tenantName.unwrap());
        }
        return exists;
    }

    @Override
    public DeletionStatus<TenantName> delete(TenantName tenantName) {
        val status = delegate.delete(tenantName);
        cache.removeFromCache(tenantName.unwrap());
        return status;
    }

    @Override
    public Tenant findOne(TenantName tenantName) {
        val tenant = delegate.findOne(tenantName);
        cache.addToCache(tenant.getTenantName());
        return tenant;
    }

    @Override
    public SortedMap<CloudProviderType, ? extends SortedMap<String, StreamingRegionInfo>> findAllRegions() {
        return delegate.findAllRegions();
    }

    @Override
    public Set<CloudProviderType> findAvailableClouds() {
        return delegate.findAvailableClouds();
    }

    @Override
    public CloudProviderType findCloudForRegion(Optional<CloudProviderType> cloud, RegionName region) {
        return delegate.findCloudForRegion(cloud, region);
    }

    @Override
    public CreationStatus<Tenant> create(TenantName tenantName, Either<String, Pair<CloudProviderType, RegionName>> clusterOrCloud, String plan, String userEmail) {
        val status = delegate.create(tenantName, clusterOrCloud, plan, userEmail);
        cache.addToCache(tenantName.unwrap());
        return status;
    }
}
