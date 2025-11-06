package com.dtsx.astra.cli.operations.db.region;

import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.FoundRegion;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.stream.Stream;

@RequiredArgsConstructor
public abstract class AbstractRegionListOperation implements Operation<Stream<FoundRegion>> {
    protected final RegionGateway regionGateway;
    protected final RegionListRequest request;

    public record FoundRegion(
        CloudProvider cloudProvider,
        String regionName,
        String displayName,
        String zone,
        boolean hasFreeTier,
        Object raw
    ) {}

    public record RegionListRequest(
        @Nullable List<String> nameFilter,
        @Nullable List<CloudProvider> cloudFilter,
        @Nullable List<String> zoneFilter
    ) {}

    protected abstract SortedMap<CloudProvider, ? extends SortedMap<String, RegionInfo>> fetchRegions();

    @Override
    public Stream<FoundRegion> execute() {
        val regions = fetchRegions();

        return regions.sequencedEntrySet()
            .reversed()
            .stream()
            .filter((e1) -> (
                e1.getValue() != null && passesCloudFilter(request.cloudFilter, e1)
            ))
            .flatMap((e1) -> e1.getValue()
                .sequencedEntrySet()
                .stream()
                .filter((e2) -> passesNameFilter(request.nameFilter, e2))
                .filter((e2) -> passesZoneFilter(request.zoneFilter, e2))
                .map((e2) -> new FoundRegion(
                    e1.getKey(),
                    e2.getKey(),
                    e2.getValue().displayName(),
                    e2.getValue().zone(),
                    e2.getValue().hasFreeTier(),
                    e2.getValue().raw()
                ))
            );
    }

    private boolean passesNameFilter(@Nullable List<String> nameFilter, Entry<String, RegionInfo> entry) {
        return nameFilter == null || nameFilter.stream().anyMatch((f) ->
            entry.getKey().toLowerCase().contains(f.toLowerCase()) || entry.getValue().displayName().toLowerCase().contains(f.toLowerCase())
        );
    }

    private boolean passesCloudFilter(@Nullable List<CloudProvider> cloudFilter, Entry<CloudProvider, ? extends SortedMap<String, RegionInfo>> entry) {
        return cloudFilter == null || cloudFilter.contains(entry.getKey());
    }

    private boolean passesZoneFilter(@Nullable List<String> zoneFilter, Entry<String, RegionInfo> entry) {
        return zoneFilter == null || zoneFilter.stream().anyMatch((z) -> entry.getValue().zone().equalsIgnoreCase(z));
    }
}
