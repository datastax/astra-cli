package com.dtsx.astra.cli.gateways.db.region;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.db.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class RegionGatewayImpl implements RegionGateway {
    private final APIProvider api;

    @Override
    public SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> findAllServerless(boolean vector) {
        val regionType = vector ? RegionType.VECTOR : RegionType.ALL;

        return AstraLogger.loading("Fetching all available " + ((vector) ? "vector" : "serverless") + " regions", (_) -> (
            api.astraOpsClient().db().regions()
                .findAllServerless(regionType)
                .collect(Collectors.toMap(
                    r -> CloudProviderType.valueOf(r.getCloudProvider()),
                    r -> new TreeMap<>() {{
                        put(r.getName(), new RegionInfo(r.getDisplayName(), !r.isReservedForQualifiedUsers(), r.getZone(), r));
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
    public SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> findAllClassic() {
        return AstraLogger.loading("Fetching all available classic regions", (_) -> (
            api.astraOpsClient().db().regions()
                .findAll()
                .collect(Collectors.toMap(
                    DatabaseRegion::getCloudProvider,
                    (r) -> new TreeMap<>() {{
                        put(
                            r.getRegion(),
                            new RegionInfo(r.getRegionDisplay(), r.getTier().equalsIgnoreCase("developer"), r.getRegionContinent(), r)
                        );
                    }},
                    (m1, m2) -> new TreeMap<>(m1) {{ // devops api duplicates regions because of course it does
                        m2.forEach((name, info) ->
                            merge(name, info, (i1, i2) ->
                                new RegionInfo(i2.displayName(), i1.hasFreeTier() || i2.hasFreeTier(), i2.zone(), i2)
                            )
                        );
                    }},
                    TreeMap::new
                ))
        ));
    }

    @Override
    public List<Datacenter> findAllForDb(DbRef dbRef) {
        return AstraLogger.loading("Fetching regions for db " + highlight(dbRef), (_) -> (
            api.dbOpsClient(dbRef).datacenters().findAll().toList()
        ));
    }

    @Override
    public Optional<Datacenter> findOneForDb(DbRef dbRef, RegionName regionName) {
        return AstraLogger.loading("Fetching region " + highlight(regionName) + " for db " + highlight(dbRef), (_) -> (
            findAllForDb(dbRef).stream()
                .filter(dc -> dc.getRegion().equalsIgnoreCase(regionName.unwrap()))
                .findFirst()
        ));
    }

    @Override
    public Set<CloudProviderType> findAvailableClouds() {
        return AstraLogger.loading("Finding cloud providers for all available regions", (_) -> (
            api.astraOpsClient().db().regions()
                .findAllServerless(RegionType.ALL)
                .map(DatabaseRegionServerless::getCloudProvider)
                .map(CloudProviderType::valueOf)
                .collect(Collectors.toSet())
        ));
    }

    @Override
    public CreationStatus<RegionName> create(DbRef ref, RegionName region, String tier, CloudProviderType cp) {
        val exists = existsInDb(ref, region);

        if (exists) {
            return CreationStatus.alreadyExists(region);
        }

        AstraLogger.loading("Creating region " + highlight(region) + " for db " + highlight(ref), (_) -> {
            api.dbOpsClient(ref).datacenters().create(tier, cp, region.unwrap());
            return null;
        });

        return CreationStatus.created(region);
    }

    @Override
    public DeletionStatus<RegionName> delete(DbRef ref, RegionName region) {
        val exists = existsInDb(ref, region);

        if (!exists) {
            return DeletionStatus.notFound(region);
        }

        AstraLogger.loading("Deleting region " + highlight(region) + " from db " + highlight(ref), (_) -> {
            api.dbOpsClient(ref).datacenters().delete(region.unwrap());
            return null;
        });

        return DeletionStatus.deleted(region);
    }

    private boolean existsInDb(DbRef dbRef, RegionName region) {
        return AstraLogger.loading("Checking if region " + highlight(region) + " exists in db " + highlight(dbRef), (_) -> (
            findAllForDb(dbRef).stream().anyMatch(dc -> dc.getRegion().equalsIgnoreCase(region.unwrap()))
        ));
    }
}
