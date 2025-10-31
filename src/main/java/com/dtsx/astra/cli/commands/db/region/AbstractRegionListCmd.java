package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.CliConstants.$Cloud;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.FoundRegion;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.utils.Collectionutils.sequencedMapOf;

public abstract class AbstractRegionListCmd extends AbstractRegionCmd<Stream<FoundRegion>> {
    @Option(
        names = { "-f", "--filter" },
        description = "Comma-separated case-insensitive partial-match filters on the region name",
        paramLabel = "FILTER",
        split = ","
    )
    public @Nullable List<String> $nameFilter;

    @Option(
        names = { $Cloud.LONG, $Cloud.SHORT },
        description = "Comma-separated list of cloud providers to filter on",
        paramLabel = "FILTER",
        split = ","
    )
    public @Nullable List<CloudProviderType> $cloudFilter;

    @Option(
        names = { "-z", "--zone" },
        description = "Comma-separated list of zones to include",
        paramLabel = "FILTER",
        split = ","
    )
    public @Nullable List<String> $zoneFilter;

    @Override
    protected final OutputJson executeJson(Supplier<Stream<FoundRegion>> regions) {
        val data = regions.get()
            .map((r) -> sequencedMapOf(
                "cloudProvider", r.cloudProvider().name(),
                "region", r.raw(),
                "hasFreeTier", r.hasFreeTier()
            ))
            .toList();

        return OutputJson.serializeValue(data);
    }

    @Override
    protected final OutputAll execute(Supplier<Stream<FoundRegion>> regions) {
        val data = regions.get()
            .map((r) -> sequencedMapOf(
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
            return cp + ShellTable.highlight(ctx, " (free)");
        }

        return cp;
    }
}
