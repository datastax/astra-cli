package com.dtsx.astra.cli.commands.pcu.associations;

import com.dtsx.astra.cli.core.completions.impls.PcuAssocTargetsCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.DatacenterId;
import com.dtsx.astra.cli.core.models.PcuAssocTarget;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.associations.PcuDisassociateOperation;
import com.dtsx.astra.cli.operations.pcu.associations.PcuDisassociateOperation.*;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.ASSOCIATION_ALREADY_EXISTS;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "disassociate",
    description = "Disassociate a PCU group to a datacenter"
)
@Example(
    comment = "Disassociate a specific datacenter from a PCU group",
    command = "${cli.name} pcu disassociate my_pcu ee6991af-7783-4de9-8da0-c7fa250c89e2-1"
)
@Example(
    comment = "Disassociate a database with a single datacenter from a PCU group",
    command = "${cli.name} pcu disassociate my_pcu my_database"
)
public class PcuDisassociateCmd extends AbstractPcuAssociationRequirePcuCmd<AssociationDeleteResult> {
    @Parameters(
        paramLabel = "TARGET",
        completionCandidates = PcuAssocTargetsCompletion.class,
        description = "PCU association target (database name/id or datacenter id)",
        index = "1"
    )
    public PcuAssocTarget $target;

    @Option(
        names = { "--if-exists" },
        description = "Don't error if the association doesn't exist"
    )
    public boolean $ifExits;

    @Override
    protected final OutputAll execute(Supplier<AssociationDeleteResult> result) {
        return switch (result.get()) {
            case AssociationDeleted(var dcId) -> handleAssociationDeleted(dcId);
            case AssociationNotFound(var dcId) -> handleAssociationNotFound(dcId);
            case AssociationIllegallyNotFound(var dcId) -> throwAssociationNotFound(dcId);
        };
    }

    private OutputAll handleAssociationDeleted(DatacenterId dcId) {
        val message = "Datacenter @!%s!@ has been disassociated from PCU group @!%s!@.".formatted(
            dcId,
            $pcuRef
        );

        return OutputAll.response(message, mkData(dcId, true));
    }

    private OutputAll handleAssociationNotFound(DatacenterId dcId) {
        val message = "Datacenter @!%s!@ was not actually associated with PCU group @!%s!@.".formatted(
            dcId,
            $pcuRef
        );

        return OutputAll.response(message, mkData(dcId, false));
    }

    private <T> T throwAssociationNotFound(DatacenterId dcId) {
        throw new AstraCliException(ASSOCIATION_ALREADY_EXISTS, """
          @|bold,red Error: Datacenter %s was not actually associated with PCU group %s.|@

          To ignore this error, provide the @'!--if-not-exists!@ flag to skip this error if the association didn't exist.
        """.formatted(
            dcId,
            $pcuRef
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-not-exists")
        ));
    }

    private LinkedHashMap<String, Object> mkData(DatacenterId dcId, Boolean wasDeleted) {
        return sequencedMapOf(
            "targetId", dcId,
            "targetType", "datacenter",
            "wasDeleted", wasDeleted
        );
    }

    @Override
    protected Operation<AssociationDeleteResult> mkOperation() {
        return new PcuDisassociateOperation(dbGateway, associationsGateway, new PcuDisassociateRequest($pcuRef, $target, $ifExits));
    }
}
