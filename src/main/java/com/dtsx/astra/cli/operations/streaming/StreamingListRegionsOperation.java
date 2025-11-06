package com.dtsx.astra.cli.operations.streaming;

import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingListRegionsOperation.FoundRegion;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class StreamingListRegionsOperation implements Operation<Stream<FoundRegion>> {
    private final StreamingGateway streamingGateway;
    private final RegionListRequest request;

    public record FoundRegion(
        String cloudProvider,
        String regionName,
        String displayName,
        boolean isPremium,
        Object raw
    ) {}

    public record RegionListRequest(
        @Nullable List<String> nameFilter,
        @Nullable List<CloudProvider> cloudFilter
    ) {}

    @Override
    public Stream<FoundRegion> execute() {
        val regions = streamingGateway.findAllRegions();
        
        return regions.entrySet().stream()
            .filter(entry -> passesCloudFilter(request.cloudFilter, entry.getKey()))
            .flatMap(entry -> {
                val cloudProvider = entry.getKey();
                val regionMap = entry.getValue();
                
                return regionMap.entrySet().stream()
                    .filter(regionEntry -> passesNameFilter(request.nameFilter, regionEntry.getKey(), regionEntry.getValue().displayName()))
                    .map(regionEntry -> new FoundRegion(
                        cloudProvider.name().toLowerCase(),
                        regionEntry.getKey(),
                        regionEntry.getValue().displayName(),
                        regionEntry.getValue().isPremium(),
                        regionEntry.getValue().raw()
                    ));
            });
    }

    private boolean passesCloudFilter(@Nullable List<CloudProvider> cloudFilter, CloudProvider cloudProvider) {
        return cloudFilter == null || cloudFilter.contains(cloudProvider);
    }

    private boolean passesNameFilter(@Nullable List<String> nameFilter, String regionName, String displayName) {
        return nameFilter == null || nameFilter.stream()
            .anyMatch(filter -> 
                regionName.toLowerCase().contains(filter.toLowerCase()) ||
                displayName.toLowerCase().contains(filter.toLowerCase())
            );
    }
}
