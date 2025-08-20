package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingListRegionsOperation;
import com.dtsx.astra.cli.operations.streaming.StreamingListRegionsOperation.FoundRegion;
import com.dtsx.astra.cli.operations.streaming.StreamingListRegionsOperation.RegionListRequest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Command(
    name = "list-regions",
    description = "List all available regions for Astra streaming tenants"
)
@Example(
    comment = "List all available regions for streaming tenants",
    command = "astra streaming list-regions"
)
@Example(
    comment = "Filter by cloud provider",
    command = "astra streaming list-regions --cloud aws,gcp"
)
@Example(
    comment = "Filter by partial region name",
    command = "astra streaming list-regions --filter us-"
)
public class StreamingListRegionsCmd extends AbstractStreamingCmd<Stream<FoundRegion>> {
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

    @Override
    protected OutputJson executeJson(Supplier<Stream<FoundRegion>> regions) {
        val data = regions.get()
            .map((r) -> Map.of(
                "cloudProvider", r.cloudProvider(),
                "region", r.regionName(),
                "displayName", r.displayName(),
                "isPremium", r.isPremium()
            ))
            .toList();

        return OutputJson.serializeValue(data);
    }

    @Override
    protected OutputAll execute(Supplier<Stream<FoundRegion>> regions) {
        val data = regions.get()
            .map((r) -> Map.of(
                "Cloud Provider", formatCloudProviderName(r.cloudProvider(), r.isPremium()),
                "Region", r.regionName(),
                "Display Name", r.displayName()
            ))
            .toList();

        return new ShellTable(data).withColumns("Cloud Provider", "Region", "Display Name");
    }

    private String formatCloudProviderName(String cloudProvider, boolean isPremium) {
        val cp = cloudProvider.toLowerCase();

        if (isPremium) {
            return cp + ShellTable.highlight(" (premium)");
        }

        return cp;
    }

    @Override
    protected Operation<Stream<FoundRegion>> mkOperation() {
        return new StreamingListRegionsOperation(streamingGateway, new RegionListRequest(
            $nameFilter,
            $cloudFilter
        ));
    }
}
