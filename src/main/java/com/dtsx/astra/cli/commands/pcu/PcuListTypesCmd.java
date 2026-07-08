package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.RegionRef;
import com.dtsx.astra.cli.core.output.PlatformChars;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.pcu.PcuListTypesOperation;
import com.dtsx.astra.cli.operations.pcu.PcuListTypesOperation.PcuListTypesRequest;
import com.dtsx.astra.sdk.pcu.domain.PCUType;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.core.CliConstants.$Cloud;
import static com.dtsx.astra.cli.core.CliConstants.$Regions;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "list-types",
    description = "List available PCU types"
)
@Example(
    comment = "List all PCU types",
    command = "${cli.name} pcu list-types"
)
public class PcuListTypesCmd extends AbstractPcuCmd<Stream<PCUType>> {
    @Option(
        names = { $Cloud.LONG, $Cloud.SHORT },
        description = "Cloud provider filter"
    )
    public Optional<CloudProvider> $cloudProvider;

    @Option(
        names = { $Regions.LONG, $Regions.SHORT },
        description = "Region filter"
    )
    public Optional<String> $regionName;

    @Override
    protected final OutputJson executeJson(Supplier<Stream<PCUType>> result) {
        return OutputJson.serializeValue(result.get().toList());
    }

    @Override
    protected final OutputAll execute(Supplier<Stream<PCUType>> result) {
        val data = result.get()
            .map((pcuType) -> sequencedMapOf(
                "Type", pcuType.getType(),
                "Region", pcuType.getRegion(),
                "Cloud Provider", pcuType.getProvider(),
                "Enabled", pcuType.isEnabled() ? PlatformChars.presenceIndicator(ctx.isWindows()) : ""
            ))
            .toList();

        return new ShellTable(data).withColumns("Type", "Cloud Provider", "Region", "Enabled");
    }

    @Override
    protected PcuListTypesOperation mkOperation() {
        val req = new PcuListTypesRequest(
            $cloudProvider,
            $regionName.map(RegionRef::mustParse)
        );
        return new PcuListTypesOperation(pcuGateway, req);
    }
}
