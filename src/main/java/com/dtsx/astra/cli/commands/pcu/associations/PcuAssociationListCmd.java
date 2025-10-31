package com.dtsx.astra.cli.commands.pcu.associations;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.associations.PcuAssociationsListOperation;
import com.dtsx.astra.cli.operations.pcu.associations.PcuAssociationsListOperation.PcuAssociationsListRequest;
import com.dtsx.astra.cli.operations.pcu.associations.PcuAssociationsListOperation.PcuAssociationsListResult;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.utils.Collectionutils.sequencedMapOf;

@Command(
    name = "list-associations",
    description = "List the associations of a PCU group"
)
@Example(
    comment = "List the associations of a PCU group",
    command = "${cli.name} pcu list-associations my_pu"
)
public class PcuAssociationListCmd extends AbstractPcuAssociationPromptForPcuCmd<Stream<PcuAssociationsListResult>> {
    @Option(
        names = { "--all", "-a" },
        description = "List all associations for all of your PCU groups",
        defaultValue = "false"
    )
    public boolean $all;

    @Override
    protected boolean shouldPromptForPcuRef() {
        return super.shouldPromptForPcuRef() && !this.$all;
    }

    @Override
    protected final OutputJson executeJson(Supplier<Stream<PcuAssociationsListResult>> result) {
        return OutputJson.serializeValue(result.get().toList());
    }

    @Override
    protected final OutputAll execute(Supplier<Stream<PcuAssociationsListResult>> result) {
        val data = result.get()
            .flatMap((res) ->
                res.associations()
                    .map((assoc) -> sequencedMapOf(
                        "PCU Group", Objects.requireNonNullElse(res.pcuGroup().getTitle(), "n/a"),
                        "PCU Group ID", res.pcuGroup().getId(),
//                        "Type", "DC", // maybe need to add if streaming PCUs later
                        "Target", assoc.getClusterName(),
                        "Target ID", assoc.getDatacenterUUID()
                    ))
            )
            .toList();

        if ($all) {
            return new ShellTable(data).withColumns("PCU Group", "PCU Group ID", "Target", "Target ID");
        } else {
            return new ShellTable(data).withColumns("Target", "Target ID");
        }
    }

    @Override
    protected Operation<Stream<PcuAssociationsListResult>> mkOperation() {
        return new PcuAssociationsListOperation(pcuGateway, associationsGateway, new PcuAssociationsListRequest($pcuRef, $all));
    }

    @Override
    protected String pcuRefPrompt() {
        return "Select the PCU group to list its associated datacenters";
    }
}
