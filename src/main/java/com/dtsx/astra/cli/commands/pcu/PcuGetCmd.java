package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.PcuGetOperation;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.pcu.PcuGetOperation.*;

@Command(
    name = "get",
    aliases = { "describe" },
    description = "Get information about a specific PCU group."
)
@Example(
    comment = "Get information about a specific PCU group",
    command = "${cli.name} pcu get my_pcu"
)
@Example(
    comment = "Get a specific attribute of a PCU group",
    command = "${cli.name} pcu get my_pcu --key id"
)
public class PcuGetCmd extends AbstractPromptForPcuCmd<PcuInfo> {
    public enum PcuGetKeys {
        title,
        description,
        id,
        status,
        cloud,
        region,
        type,
        min,
        max,
        reserved,
    }

    @Option(
        names = { "-k", "--key" },
        description = "Specific PCU group attribute to retrieve",
        paramLabel = "KEY"
    )
    public Optional<PcuGetKeys> $key;

    @Override
    protected final OutputJson executeJson(Supplier<PcuInfo> result) {
        return switch (result.get()) {
            case PcuInfoFull info -> OutputJson.serializeValue(info.pcuGroup());
            case PcuInfoValue(var value) -> OutputJson.serializeValue(value);
        };
    }

    @Override
    protected final OutputAll execute(Supplier<PcuInfo> result) {
        return switch (result.get()) {
            case PcuInfoFull info -> mkTable(info.pcuGroup());
            case PcuInfoValue(var value) -> OutputAll.serializeValue(value);
        };
    }

    private RenderableShellTable mkTable(PcuGroup pcuInfo) {
        return ShellTable.forAttributes(new LinkedHashMap<>() {{
            put("Title", Objects.requireNonNullElse(pcuInfo.getTitle(), "n/a"));
            put("ID", pcuInfo.getId());
            put("Cloud Provider", pcuInfo.getCloudProvider());
            put("Region", pcuInfo.getRegion());
            put("Status", pcuInfo.getStatus());
            put("Min", pcuInfo.getMin());
            put("Max", pcuInfo.getMax());
            put("Reserved", pcuInfo.getReserved());
        }});
    }

    @Override
    protected Operation<PcuInfo> mkOperation() {
        return new PcuGetOperation(pcuGateway, new PcuGetRequest($pcuRef, $key));
    }

    @Override
    protected String pcuRefPrompt() {
        return "Select the PCU group to get information about";
    }
}
