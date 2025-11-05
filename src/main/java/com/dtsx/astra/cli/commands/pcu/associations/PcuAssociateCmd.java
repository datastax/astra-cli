package com.dtsx.astra.cli.commands.pcu.associations;

import com.dtsx.astra.cli.core.completions.impls.PcuAssocTargetsCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.DatacenterId;
import com.dtsx.astra.cli.core.models.PcuAssocTarget;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.associations.PcuAssociateOperation;
import com.dtsx.astra.cli.operations.pcu.associations.PcuAssociateOperation.*;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.ASSOCIATION_ALREADY_EXISTS;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "associate",
    description = "Associate a PCU group to a datacenter"
)
@Example(
    comment = "Associate a specific datacenter to a PCU group",
    command = "${cli.name} pcu associate my_pcu ee6991af-7783-4de9-8da0-c7fa250c89e2-1"
)
@Example(
    comment = "Associate a database with a single datacenter to a PCU group",
    command = "${cli.name} pcu associate my_pcu my_database"
)
public class PcuAssociateCmd extends AbstractPcuAssociationRequirePcuCmd<AssociationCreateResult> {
    @Parameters(
        paramLabel = "TARGET",
        completionCandidates = PcuAssocTargetsCompletion.class,
        description = "PCU association target (database name/id or datacenter id)",
        index = "1"
    )
    public PcuAssocTarget $target;

    @Option(
        names = { "--if-not-exists" },
        description = "Don't error if the association already exists"
    )
    public boolean $ifNotExists;

    @Override
    protected final OutputAll execute(Supplier<AssociationCreateResult> result) {
        return switch (result.get()) {
            case AssociationCreated(var dcId) -> handleAssociationCreated(dcId);
            case AssociationAlreadyExists(var dcId) -> handleAssociationAlreadyExists(dcId);
            case AssociationIllegallyAlreadyExists(var dcId) -> throwAssociationAlreadyExists(dcId);
        };
    }

    private OutputAll handleAssociationCreated(DatacenterId dcId) {
        val message = "Datacenter @!%s!@ has been associated with PCU group @!%s!@.".formatted(
            dcId,
            $pcuRef
        );

        return OutputAll.response(message, mkData(dcId, true));
    }

    private OutputAll handleAssociationAlreadyExists(DatacenterId dcId) {
        val message = "Datacenter @!%s!@ is already associated with PCU group @!%s!@.".formatted(
            dcId,
            $pcuRef
        );

        return OutputAll.response(message, mkData(dcId, false));
    }

    private <T> T throwAssociationAlreadyExists(DatacenterId dcId) {
        throw new AstraCliException(ASSOCIATION_ALREADY_EXISTS, """
          @|bold,red Error: Datacenter %s is already associated with PCU group %s.|@

          To ignore this error, provide the @'!--if-not-exists!@ flag to skip this error if the association already exists.
        """.formatted(
            dcId,
            $pcuRef
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-not-exists")
        ));
    }

    private LinkedHashMap<String, Object> mkData(DatacenterId dcId, Boolean wasCreated) {
        return sequencedMapOf(
            "targetId", dcId,
            "targetType", "datacenter",
            "wasCreated", wasCreated
        );
    }

    @Override
    protected Operation<AssociationCreateResult> mkOperation() {
        return new PcuAssociateOperation(dbGateway, associationsGateway, new PcuAssociateRequest($pcuRef, $target, $ifNotExists));
    }
}
