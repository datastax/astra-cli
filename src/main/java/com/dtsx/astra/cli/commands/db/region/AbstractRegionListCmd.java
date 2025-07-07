package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.FoundRegion;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractRegionListCmd extends AbstractRegionCmd<Stream<FoundRegion>> {
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
    protected OutputJson executeJson(Stream<FoundRegion> regions) {
        val data = regions
            .map((r) -> Map.of(
                "cloudProvider", r.cloudProvider().name(),
                "region", r.raw(),
                "hasFreeTier", r.hasFreeTier()
            ))
            .toList();

        return OutputJson.serializeValue(data);
    }

    @Override
    protected OutputAll execute(Stream<FoundRegion> regions) {
        val data = regions
            .map((r) -> Map.of(
                "Cloud Provider", formatCloudProviderName(r.cloudProvider(), r.hasFreeTier()),
                "Region", r.regionName(),
                "Display Name", r.displayName(),
                "Zone", r.zone()
            ))
            .toList();

        return new ShellTable(data).withColumns("Cloud Provider", "Region", "Display Name", "Zone");
    }

    private String formatCloudProviderName(CloudProviderType cloudProvider, boolean hasFreeTier) {
        val cp = cloudProvider.name().toLowerCase();

        if (hasFreeTier) {
            return cp + ShellTable.highlight(" (free)");
        }

        return cp;
    }
}
