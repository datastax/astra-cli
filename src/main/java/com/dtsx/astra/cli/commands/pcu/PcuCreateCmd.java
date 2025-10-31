package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.CliConstants.$Cloud;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupStatusType;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuProvisionType;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.PcuCreateOperation;
import com.dtsx.astra.cli.operations.pcu.PcuCreateOperation.PcuCreateResult;
import com.dtsx.astra.cli.operations.pcu.PcuCreateOperation.PcuGroupAlreadyExistsIllegallyWithStatus;
import com.dtsx.astra.cli.operations.pcu.PcuCreateOperation.PcuGroupAlreadyExistsWithStatus;
import com.dtsx.astra.cli.operations.pcu.PcuCreateOperation.PcuGroupCreated;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.PCU_GROUP_ALREADY_EXISTS;
import static com.dtsx.astra.cli.operations.pcu.PcuCreateOperation.CreatePcuRequest;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "create",
    description = "Create a new PCU group"
)
@Example(
    comment = "Create a basic pcu group",
    command = "${cli.name} pcu create my_pcu -c AWS -r us-west-2"
)
@Example(
    comment = "Create a pcu group if it doesn't already exist",
    command = "${cli.name} pcu create my_pcu --region us-east1 --if-not-exists"
)
public class PcuCreateCmd extends AbstractPcuRequiredCmd<PcuCreateResult> {
    @ArgGroup
    public @Nullable ExistingBehavior $existingBehavior;

    public static class ExistingBehavior {
        @Option(
            names = { "--if-not-exists" },
            description = "Don't error if the pcu already exists"
        )
        public boolean $ifNotExists;

        @Option(
            names = { "--allow-duplicate-names" },
            description = "Allow multiple pcus with the same name"
        )
        public boolean $allowDuplicateNames;
    }

    @ArgGroup(validate = false, heading = "%nPCU group configuration options:%n")
    public @Nullable pcuCreationOptions $pcuCreationOptions;

    public static class pcuCreationOptions {
        @Option(
            names = { $Cloud.LONG, $Cloud.SHORT },
            description = "Cloud provider this PCU will work in",
            paramLabel = $Cloud.LABEL
        )
        public CloudProviderType cloud;

        @Option(
            names = { $Regions.LONG, $Regions.SHORT },
            description = "Cloud region this PCU will work in. @|bold Use one of the `${cli.name} db list-regions-*` commands to see available regions.|@",
            paramLabel = $Regions.LABEL
        )
        public RegionName region;

        @Option(
            names = { "--description", "-d" },
            paramLabel = "DESC",
            description = "Optional description for the PCU group"
        )
        public Optional<String> description;

        @Option(
            names = { "--instance-type", "-it" },
            paramLabel = "TYPE",
            description = "Cache type for the PCU group",
            defaultValue = "shared"
        )
        public String instanceType;

        @Option(
            names = { "--provision-type", "-pt" },
            paramLabel = "TYPE",
            description = "Provision type for the PCU group",
            defaultValue = "shared"
        )
        public PcuProvisionType provisionType;

        @Option(
            names = { "--min" },
            description = "Minimum capacity units for the PCU group",
            paramLabel = "MIN",
            defaultValue = "1"
        )
        public Integer min;

        @Option(
            names = { "--max" },
            description = "Maximum capacity units for the PCU group",
            paramLabel = "MAX",
            defaultValue = "1"
        )
        public Integer max;

        @Option(
            names = { "--reserved" },
            description = "Reserved capacity units for the PCU group",
            paramLabel = "RESERVED",
            defaultValue = "0"
        )
        public Integer reserved;
    }

    @Override
    protected Operation<PcuCreateResult> mkOperation() {
        val pcuTitle = $pcuRef.fold(
            id -> { throw new OptionValidationException("pcu name", "may not provide an id (%s) when creating a new pcu group; must be a human-readable name".formatted(id.toString())); },
            name -> name
        );

        if ($pcuCreationOptions == null || $pcuCreationOptions.region == null) {
            throw new ParameterException(spec.commandLine(), "Must provide a region (via --region) when creating a new pcu group. Use the `${cli.name} db list-regions-*` commands to see available regions.");
        }

        val existingBehavior =
            ($existingBehavior != null && $existingBehavior.$ifNotExists)
                ? PcuCreateOperation.ExistingBehavior.CREATE_IF_NOT_EXISTS :
            ($existingBehavior != null && $existingBehavior.$allowDuplicateNames)
                ? PcuCreateOperation.ExistingBehavior.ALLOW_DUPLICATES
                : PcuCreateOperation.ExistingBehavior.FAIL;

        return new PcuCreateOperation(pcuGateway, new CreatePcuRequest(
            pcuTitle,
            $pcuCreationOptions.description,
            $pcuCreationOptions.cloud,
            $pcuCreationOptions.region,
            $pcuCreationOptions.instanceType,
            $pcuCreationOptions.provisionType,
            $pcuCreationOptions.min,
            $pcuCreationOptions.max,
            $pcuCreationOptions.reserved,
            existingBehavior
        ));
    }

    @Override
    protected final OutputAll execute(Supplier<PcuCreateResult> result) {
        return switch (result.get()) {
            case PcuGroupAlreadyExistsWithStatus(var pcuId, var currStatus) -> handlePcuAlreadyExistsWithStatus(pcuId, currStatus);
            case PcuGroupAlreadyExistsIllegallyWithStatus(var pcuId, var currStatus) -> throwPcuAlreadyExistsWithStatus(pcuId, currStatus);
            case PcuGroupCreated(var pcuId) -> handlePcuCreated(pcuId);
        };
    }

    private OutputAll handlePcuAlreadyExistsWithStatus(UUID pcuId, PcuGroupStatusType currStatus) {
        val message = "PCU group %s already exists with id %s, and has status %s.".formatted(
            ctx.highlight($pcuRef),
            ctx.highlight(pcuId),
            ctx.highlight(currStatus)
        );

        val data = mkData(pcuId, false, currStatus);

        return OutputAll.response(message, data, List.of(
            new Hint("Get information about the existing group:", "${cli.name} pcu get %s".formatted($pcuRef))
        ));
    }

    private <T> T throwPcuAlreadyExistsWithStatus(UUID pcuId, PcuGroupStatusType currStatus) {
        throw new AstraCliException(PCU_GROUP_ALREADY_EXISTS, """
          @|bold,red Error: PCU group %s already exists with id %s, and has status %s.|@
        
          To ignore this error, either:
          - Provide the @'!--if-not-exists!@ flag to skip this error if the group already exists.
          - Provide the @'!--allow-duplicate-names!@ flag to create the group even if another with the same name already exists.
        """.formatted(
            $pcuRef,
            pcuId,
            currStatus
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-not-exists"),
            new Hint("Example fix:", originalArgs(), "--allow-duplicate-names"),
            new Hint("Get information about the existing pcu:", "${cli.name} pcu get %s".formatted($pcuRef))
        ));
    }

    private OutputAll handlePcuCreated(UUID pcuId) {
        val message = "PCU group %s has been created with id %s.".formatted(
            ctx.highlight($pcuRef),
            ctx.highlight(pcuId)
        );

        val data = mkData(pcuId, true, PcuGroupStatusType.CREATED);

        return OutputAll.response(message, data, List.of(
            new Hint("Get more information about the new group:", "${cli.name} pcu get %s".formatted($pcuRef))
        ));
    }

    private LinkedHashMap<String, Object> mkData(UUID pcuId, Boolean wasCreated, PcuGroupStatusType currentStatus) {
        return sequencedMapOf(
            "pcuId", pcuId,
            "wasCreated", wasCreated,
            "currentStatus", currentStatus
        );
    }
}
