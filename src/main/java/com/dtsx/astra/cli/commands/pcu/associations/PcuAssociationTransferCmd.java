package com.dtsx.astra.cli.commands.pcu.associations;

import com.dtsx.astra.cli.core.CliConstants.$Pcu;
import com.dtsx.astra.cli.core.completions.impls.PcuAssocTargetsCompletion;
import com.dtsx.astra.cli.core.completions.impls.PcuGroupsCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.DatacenterId;
import com.dtsx.astra.cli.core.models.PcuAssocTarget;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.associations.PcuAssociationTransferOperation;
import com.dtsx.astra.cli.operations.pcu.associations.PcuAssociationTransferOperation.*;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.*;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "transfer-association",
    description = "Transfer an association from one PCU group to another"
)
@Example(
    comment = "Transfer a datacenter association association from one PCU group to another",
    command = "${cli.name} pcu transfer-association ee6991af-7783-4de9-8da0-c7fa250c89e2-1 --from pcu_1 --to pcu_2"
)
public class PcuAssociationTransferCmd extends AbstractPcuAssociationCmd<AssociationTransferResult> {
    @Parameters(
        paramLabel = "TARGET",
        completionCandidates = PcuAssocTargetsCompletion.class,
        description = "PCU association target (database name/id or datacenter id)",
        index = "0"
    )
    public PcuAssocTarget $target;

    @ArgGroup
    public SourceBehavior $sourceBehavior;

    public static class SourceBehavior {
        @Option(
            names = { "--from", "-f" },
            completionCandidates = PcuGroupsCompletion.class,
            description = "The PCU group which initially holds the association. May be omitted to let the CLI automatically detect it",
            paramLabel = $Pcu.LABEL
        )
        protected PcuRef $from;

        @Option(
            names = { "--or-create" },
            description = "If the datacenter is not already associated to a PCU group, create the association first"
        )
        public boolean $orCreate;
    }

    @Option(
        names = { "--to", "-t" },
        completionCandidates = PcuGroupsCompletion.class,
        description = "The PCU group to transfer the association to",
        paramLabel = $Pcu.LABEL,
        required = true
    )
    protected PcuRef $to;

    @Option(
        names = { "--if-not-exists" },
        description = "Don't error if the association already exists"
    )
    public boolean $ifNotExists;

    @Override
    protected final OutputAll execute(Supplier<AssociationTransferResult> result) {
        return switch (result.get()) {
            case AssociationTransferred(var fromId, var fromRef, var toId, var target) -> handleAssociationTransferred(fromId, fromRef, toId, target);
            case AssociationCreated(var to, var target) -> handleAssociationCreated(to, target);
            case AlreadyAssociatedToTarget(var pcuId, var target) -> handleAlreadyAssociatedToTarget(pcuId, target);
            case IllegallyAlreadyAssociatedToTarget(var _, var target) -> throwAlreadyAssociatedToTarget(target);
            case CouldNotDetectSource(var target) -> throwCouldNotDetectSource(target);
            case GivenSourceNotAssociated(var given, var actual, var target) -> throwGivenSourceNotAssociated(given, actual, target);
        };
    }

    private OutputAll handleAssociationTransferred(UUID fromId, PcuRef fromRef, UUID to, DatacenterId dcId) {
        val message = "Datacenter @!%s!@ has been transferred from PCU group @!%s!@ to PCU group @!%s!@.".formatted(
            dcId,
            fromRef,
            $to
        );

        return OutputAll.response(message, mkData(fromId, to, dcId, true, false));
    }

    private OutputAll handleAssociationCreated(UUID to, DatacenterId dcId) {
        val message = "Datacenter @!%s!@ has been newly associated with PCU group @!%s!@.".formatted(
            dcId,
            $to
        );

        return OutputAll.response(message, mkData(null, to, dcId, false, true));
    }

    private OutputAll handleAlreadyAssociatedToTarget(UUID toId, DatacenterId dcId) {
        val message = "Datacenter @!%s!@ is already associated with PCU group @!%s!@; no action was taken.".formatted(
            dcId,
            $to
        );

        return OutputAll.response(message, mkData(null, toId, dcId, false, false));
    }

    private <T> T throwAlreadyAssociatedToTarget(DatacenterId dcId) {
        throw new AstraCliException(ExitCode.ASSOCIATION_ALREADY_EXISTS, """
          @|bold,red Error: Datacenter '%s' is already associated with PCU group '%s'.|@
    
          To ignore this error, provide the @'!--if-not-exists!@ flag to skip this error if the association already exists.
        """.formatted(
            dcId,
            $to
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-not-exists")
        ));
    }

    private <T> T throwCouldNotDetectSource(DatacenterId dcId) {
        throw new AstraCliException(ExitCode.ASSOCIATION_NOT_FOUND, """
          @|bold,red Error: The datacenter '%s' does not appear to be associated with any PCU group.|@
        
          To instead create a new association, you may either:
          - Provide the @'!--or-create!@ flag to automatically create the association if none exists.
          - Use the @'pcu associate!@ command to create a new association directly.
        """.formatted(
            dcId
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--or-create"),
            new Hint("Example fix:", "${cli.name} pcu associate %s %s".formatted($to, $target))
        ));
    }

    private <T> T throwGivenSourceNotAssociated(UUID given, Optional<UUID> actual, DatacenterId dcId) {
        val hints = new ArrayList<Hint>();

        if (actual.isPresent()) {
            val args1 = new ArrayList<>(originalArgs());
            val args2 = new ArrayList<>(originalArgs());

            val fromIndex = args1.indexOf("--from");

            args1.remove(fromIndex);
            args1.remove(fromIndex); // remove the value as well

            args2.set(fromIndex + 1, actual.get().toString());

            hints.add(new Hint("Let the CLI automatically detect the source PCU group:", args1, ""));
            hints.add(new Hint("Use the actual associated PCU group as the source:", args2, ""));
        } else {
            hints.add(new Hint("Let the CLI create the association as none exists:", originalArgs(), "--or-create"));
        }

        throw new AstraCliException(ExitCode.ASSOCIATION_NOT_FOUND, """
          @|bold,red Error: The given source PCU group '%s' is not associated with datacenter '%s'.|@
        
          %s
        """.formatted(
            given,
            dcId,
            (actual.isPresent())
                ? "It appears to be associated with PCU group @'!%s!@ instead.".formatted(actual)
                : "It appears to not be associated with any PCU group."
        ), hints);
    }

    private LinkedHashMap<String, Object> mkData(@Nullable UUID from, UUID to, DatacenterId dcId, Boolean wasTransferred, Boolean wasCreated) {
        return sequencedMapOf(
            "fromPcuId", from,
            "toPcuId", to,
            "targetId", dcId,
            "targetType", "datacenter",
            "wasTransferred", wasTransferred,
            "wasCreated", wasCreated
        );
    }

    @Override
    protected Operation<AssociationTransferResult> mkOperation() {
        return new PcuAssociationTransferOperation(ctx, dbGateway, pcuGateway, associationsGateway, new PcuTransferRequest(
            $to,
            $target,
            ($sourceBehavior == null)
                ? new AutodetectAndRequireSource() :
            ($sourceBehavior.$from != null)
                ? new UseSource($sourceBehavior.$from) :
            ($sourceBehavior.$orCreate)
                ? new AutodetectSourceOrCreate()
                : new AutodetectAndRequireSource(),
            $ifNotExists
        ));
    }
}
