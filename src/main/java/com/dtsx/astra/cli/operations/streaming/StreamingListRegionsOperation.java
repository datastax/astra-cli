package com.dtsx.astra.cli.operations.streaming;

import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingListRegionsOperation.FoundRegion;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
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
        boolean isPremium
    ) {}

    public record RegionListRequest(
        @Nullable List<String> nameFilter,
        @Nullable List<CloudProviderType> cloudFilter
    ) {}

    @Override
    public Stream<FoundRegion> execute() { // TODO: raw json version
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
                        regionEntry.getValue().isPremium()
                    ));
            });
    }

    private boolean passesCloudFilter(@Nullable List<CloudProviderType> cloudFilter, CloudProviderType cloudProvider) {
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
