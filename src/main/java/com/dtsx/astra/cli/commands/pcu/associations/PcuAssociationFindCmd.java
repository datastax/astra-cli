package com.dtsx.astra.cli.commands.pcu.associations;

import com.dtsx.astra.cli.core.completions.impls.PcuAssocTargetsCompletion;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.PcuAssocTarget;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.associations.PcuAssociationFindOperation;
import com.dtsx.astra.sdk.pcu.domain.PCUGroup;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "find-association",
    description = "Find the PCU group associated with a datacenter"
)
@Example(
    comment = "Find the PCU group associated with a datacenter",
    command = "${cli.name} pcu find-association ee6991af-7783-4de9-8da0-c7fa250c89e2-1"
)
@Example(
    comment = "Find the PCU group associated with a database",
    command = "${cli.name} pcu find-association my_database"
)
public class PcuAssociationFindCmd extends AbstractPcuAssociationCmd<Optional<PCUGroup>> {
    @Parameters(
        paramLabel = "TARGET",
        completionCandidates = PcuAssocTargetsCompletion.class,
        description = "PCU association target (database name/id/endpoint or datacenter id)",
        index = "0"
    )
    public PcuAssocTarget $target;

    @Override
    protected final OutputJson executeJson(Supplier<Optional<PCUGroup>> result) {
        val pcu = result.get().orElse(null);
        return OutputJson.serializeValue(pcu);
    }

    @Override
    protected final OutputAll execute(Supplier<Optional<PCUGroup>> result) {
        val pcuGroupOpt = result.get();
        if (pcuGroupOpt.isEmpty()) {
            return new ShellTable(List.of()).withColumns("PCU Group", "PCU Group ID");
        }
        
        val pcuGroup = pcuGroupOpt.get();
        val data = List.of(sequencedMapOf(
            "PCU Group", Objects.requireNonNullElse(pcuGroup.getTitle(), "n/a"),
            "PCU Group ID", pcuGroup.getId()
        ));

        return new ShellTable(data).withColumns("PCU Group", "PCU Group ID");
    }

    @Override
    protected Operation<Optional<PCUGroup>> mkOperation() {
        return new PcuAssociationFindOperation(dbGateway, associationsGateway, $target);
    }
}
