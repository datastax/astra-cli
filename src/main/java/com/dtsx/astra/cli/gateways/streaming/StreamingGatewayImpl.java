package com.dtsx.astra.cli.gateways.streaming;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.exceptions.internal.streaming.role.TenantNotFoundException;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.streaming.domain.CreateTenant;
import com.dtsx.astra.sdk.streaming.domain.StreamingRegion;
import com.dtsx.astra.sdk.streaming.domain.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.graalvm.collections.Pair;

import java.util.Map.Entry;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class StreamingGatewayImpl implements StreamingGateway {
    private final CliContext ctx;
    private final APIProvider apiProvider;

    @Override
    public Tenant findOne(TenantName tenantName) {
        return ctx.log().loading("Fetching streaming tenant " + tenantName, (_) -> {
            return apiProvider.astraOpsClient().streaming().find(tenantName.unwrap()).orElseThrow(() -> new TenantNotFoundException(tenantName));
        });
    }

    @Override
    public Stream<Tenant> findAll() {
        return ctx.log().loading("Fetching all streaming tenants", (_) -> {
            return apiProvider.astraOpsClient().streaming().findAll();
        });
    }

    @Override
    public boolean exists(TenantName tenantName) {
        return ctx.log().loading("Checking if streaming tenant " + tenantName + " exists", (_) -> {
            return apiProvider.astraOpsClient().streaming().exist(tenantName.unwrap());
        });
    }

    @Override
    public DeletionStatus<TenantName> delete(TenantName tenantName) {
        val exists = exists(tenantName);
        
        if (!exists) {
            return DeletionStatus.notFound(tenantName);
        }
        
        ctx.log().loading("Deleting streaming tenant " + tenantName, (_) -> {
            apiProvider.astraOpsClient().streaming().delete(tenantName.unwrap());
            return null;
        });
        
        return DeletionStatus.deleted(tenantName);
    }

    @Override
    public SortedMap<CloudProvider, ? extends SortedMap<String, StreamingRegionInfo>> findAllRegions() {
        return ctx.log().loading("Fetching streaming regions", (_) -> (
            apiProvider.astraOpsClient().streaming().regions()
                .findAllServerless()
                .collect(Collectors.toMap(
                    r -> CloudProvider.fromString(r.getCloudProvider()),
                    r -> new TreeMap<>() {{
                        put(r.getName(), new StreamingRegionInfo(r.getDisplayName(), r.getClassification().equalsIgnoreCase("premium"), r));
                    }},
                    (a, b) -> new TreeMap<>() {{
                        putAll(a);
                        putAll(b);
                    }},
                    TreeMap::new
                ))
        ));
    }

    @Override
    public SortedSet<CloudProvider> findAvailableClouds() {
        return ctx.log().loading("Finding cloud providers for all available streaming regions", (_) -> (
            apiProvider.astraOpsClient().streaming().regions().findAllServerless()
                .map(StreamingRegion::getCloudProvider)
                .map(CloudProvider::fromString)
                .collect(Collectors.toCollection(TreeSet::new))
        ));
    }

    @Override
    public CloudProvider findCloudForRegion(Optional<CloudProvider> cloud, RegionName region) {
        val cloudRegions = findAllRegions();

        if (cloud.isPresent()) {
            val cloudName = cloud.get().name().toLowerCase();

            if (!cloudRegions.containsKey(cloud.get())) {
                throw new OptionValidationException("cloud", "Cloud provider '%s' does not have any available streaming regions".formatted(cloudName));
            }

            if (!cloudRegions.get(cloud.get()).containsKey(region.unwrap().toLowerCase())) {
                throw new OptionValidationException("region", "Region '%s' is not available for cloud provider '%s'".formatted(region, cloud.get()));
            }

            return cloud.get();
        }

        val matchingClouds = cloudRegions.entrySet().stream()
            .filter(entry -> entry.getValue().containsKey(region.unwrap().toLowerCase()))
            .map(Entry::getKey)
            .toList();

        return switch (matchingClouds.size()) {
            case 0 ->
                throw new OptionValidationException("region", "Region '%s' is not available for any cloud provider".formatted(region));
            case 1 ->
                matchingClouds.getFirst();
            default ->
                throw new OptionValidationException("region", "Region '%s' is available for multiple cloud providers: %s".formatted(
                    region, matchingClouds.stream().map(CloudProvider::name).toList()
                ));
        };
    }

    @Override
    public CreationStatus<Tenant> create(TenantName tenantName, Either<String, Pair<CloudProvider, RegionName>> clusterOrCloud, String plan, String userEmail) {
        val exists = exists(tenantName);
        
        if (exists) {
            val tenant = findOne(tenantName);
            return CreationStatus.alreadyExists(tenant);
        }
        
        val createTenantBuilder = CreateTenant.builder()
            .tenantName(tenantName.unwrap())
            .plan(plan)
            .userEmail(userEmail);

        if (clusterOrCloud.isLeft()) {
            createTenantBuilder
                .clusterName(clusterOrCloud.getLeft());
        } else {
            createTenantBuilder
                .cloudProvider(clusterOrCloud.getRight().getLeft().name())
                .cloudRegion(clusterOrCloud.getRight().getRight().unwrap());
        }

        val createTenant = createTenantBuilder.build();
        
        ctx.log().loading("Creating streaming tenant " + tenantName, (_) -> {
            apiProvider.astraOpsClient().streaming().create(createTenant);
            return null;
        });
        
        val newTenant = findOne(tenantName);
        return CreationStatus.created(newTenant);
    }
}
