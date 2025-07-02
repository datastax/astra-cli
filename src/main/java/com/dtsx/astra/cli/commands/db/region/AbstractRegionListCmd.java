package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

public abstract class AbstractRegionListCmd extends AbstractRegionCmd<SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>>> {
    @Option(
        names = { "-f", "--filter" },
        description = "Comma-separated case-insensitive partial-match filters on the region name",
        split = ",",
        paramLabel = "FILTER"
    )
    public @Nullable List<String> $nameFilter;

    @Option(
        names = { "-c", "--cloud" },
        description = "Comma-separated list of cloud providers to filter on",
        split = ",",
        paramLabel = "FILTER"
    )
    public @Nullable List<CloudProviderType> $cloudFilter;

    @Option(
        names = { "-z", "--zone" },
        description = "Comma-separated list of zones to include",
        split = ",",
        paramLabel = "FILTER"
    )
    public @Nullable List<String> $zoneFilter;

    @Override
    protected OutputJson executeJson(SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> regions) {
        val data = regions.sequencedEntrySet()
            .reversed()
            .stream()
            .filter((e1) -> (
                passesCloudFilter(e1) && e1.getValue() != null &&
                    e1.getValue().sequencedEntrySet().stream().anyMatch(this::passesNameFilter) &&
                    e1.getValue().sequencedEntrySet().stream().anyMatch(this::passesZoneFilter)
            ))
            .flatMap((e1) -> e1.getValue()
                .sequencedEntrySet()
                .stream()
                .map((e2) -> Map.of(
                    "cloudProvider", formatCloudProviderName(e1.getKey(), e2),
                    "datacenter", e2.getValue().raw()
                ))
            )
            .toList();

        return OutputJson.serializeValue(data);
    }

    @Override
    protected OutputAll execute(SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> regions) {
        val data = regions.sequencedEntrySet()
            .reversed()
            .stream()
            .filter((e1) -> (
                passesCloudFilter(e1) && e1.getValue() != null &&
                    e1.getValue().sequencedEntrySet().stream().anyMatch(this::passesNameFilter) &&
                    e1.getValue().sequencedEntrySet().stream().anyMatch(this::passesZoneFilter)
            ))
            .flatMap((e1) -> e1.getValue()
                .sequencedEntrySet()
                .stream()
                .map((e2) -> Map.of(
                    "Cloud Provider", formatCloudProviderName(e1.getKey(), e2),
                    "Region", e2.getKey(),
                    "Display Name", e2.getValue().displayName(),
                    "Zone", e2.getValue().zone()
                ))
            )
            .toList();

        return new ShellTable(data).withColumns("Cloud Provider", "Region", "Display Name", "Zone");
    }

    private boolean passesCloudFilter(Entry<CloudProviderType, ? extends SortedMap<String, RegionInfo>> entry) {
        return $cloudFilter == null || !$cloudFilter.contains(entry.getKey());
    }

    private boolean passesNameFilter(Entry<String, RegionInfo> entry) {
        return $nameFilter == null || $nameFilter.stream().anyMatch((f) ->
            entry.getKey().toLowerCase().contains(f.toLowerCase()) || entry.getValue().displayName().toLowerCase().contains(f.toLowerCase())
        );
    }

    private boolean passesZoneFilter(Entry<String, RegionInfo> entry) {
        return $zoneFilter == null || $zoneFilter.stream().anyMatch((z) -> entry.getValue().zone().equalsIgnoreCase(z));
    }

    private String formatCloudProviderName(CloudProviderType cloudProvider, Map.Entry<String, RegionInfo> entry) {
        val cp = cloudProvider.name().toLowerCase();

        if (entry.getValue().hasFreeTier()) {
            return cp + ShellTable.highlight(" (free)");
        }

        return cp;
    }
}
