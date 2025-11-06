package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.CliConstants.$Cloud;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupProvisionType;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupStatusType;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.PcuUpdateOperation;
import com.dtsx.astra.cli.operations.pcu.PcuUpdateOperation.PcuGroupAlreadyExistsIllegallyWithStatus;
import com.dtsx.astra.cli.operations.pcu.PcuUpdateOperation.PcuGroupUpdated;
import com.dtsx.astra.cli.operations.pcu.PcuUpdateOperation.PcuUpdateResult;
import com.dtsx.astra.cli.operations.pcu.PcuUpdateOperation.UpdatePcuRequest;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.PCU_GROUP_ALREADY_EXISTS;

@Command(
    name = "update",
    description = "Update an existing PCU group"
)
@Example(
    comment = "Update an existing PCU group",
    command = "${cli.name} pcu update my_pcu --tile new_title --min 2 --max 5"
)
public class PcuUpdateCmd extends AbstractPromptForPcuCmd<PcuUpdateResult> {
    @Option(
        names = { "--allow-duplicate-names" },
        description = "Allow multiple pcus with the same name"
    )
    public boolean $allowDuplicateNames;

    @ArgGroup(validate = false, heading = "%nPCU group configuration options:%n")
    public @Nullable pcuCreationOptions $pcuUpdateOptions;

    public static class pcuCreationOptions {
        @Option(
            names = { "--title", "-t" },
            description = "New title for the PCU group",
            paramLabel = "TITLE"
        )
        public Optional<String> title;

        @Option(
            names = { $Cloud.LONG, $Cloud.SHORT },
            description = "Cloud provider this PCU will work in",
            paramLabel = $Cloud.LABEL
        )
        public Optional<CloudProvider> cloud;

        @Option(
            names = { $Regions.LONG, $Regions.SHORT },
            description = "Cloud region this PCU will work in. @|bold Use one of the `${cli.name} db list-regions-*` commands to see available regions.|@",
            paramLabel = $Regions.LABEL
        )
        public Optional<RegionName> region;

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
        public Optional<String> instanceType;

        @Option(
            names = { "--provision-type", "-pt" },
            paramLabel = "TYPE",
            description = "Provision type for the PCU group",
            defaultValue = "shared"
        )
        public Optional<PcuGroupProvisionType> provisionType;

        @Option(
            names = { "--min" },
            description = "Minimum capacity units for the PCU group",
            paramLabel = "MIN",
            defaultValue = "1"
        )
        public Optional<Integer> min;

        @Option(
            names = { "--max" },
            description = "Maximum capacity units for the PCU group",
            paramLabel = "MAX",
            defaultValue = "1"
        )
        public Optional<Integer> max;

        @Option(
            names = { "--reserved" },
            description = "Reserved capacity units for the PCU group",
            paramLabel = "RESERVED",
            defaultValue = "0"
        )
        public Optional<Integer> reserved;
    }

    @Override
    protected Operation<PcuUpdateResult> mkOperation() {
        if ($pcuUpdateOptions == null) {
            throw new ParameterException(spec.commandLine(), "No update options provided; at least one option to update must be specified.");
        }

        return new PcuUpdateOperation(pcuGateway, new UpdatePcuRequest(
            $pcuRef,
            $pcuUpdateOptions.title,
            $pcuUpdateOptions.description,
            $pcuUpdateOptions.cloud,
            $pcuUpdateOptions.region,
            $pcuUpdateOptions.min,
            $pcuUpdateOptions.max,
            $pcuUpdateOptions.reserved,
            $allowDuplicateNames
        ));
    }

    @Override
    protected final OutputAll execute(Supplier<PcuUpdateResult> result) {
        return switch (result.get()) {
            case PcuGroupAlreadyExistsIllegallyWithStatus(var pcuId, var currStatus) -> throwPcuAlreadyExistsWithStatus(pcuId, currStatus);
            case PcuGroupUpdated() -> handlePcuUpdated();
        };
    }

    private <T> T throwPcuAlreadyExistsWithStatus(UUID pcuId, PcuGroupStatusType currStatus) {
        assert $pcuUpdateOptions != null; // will always be true if this method is reached

        throw new AstraCliException(PCU_GROUP_ALREADY_EXISTS, """
          @|bold,red Error: PCU group %s already exists with id %s, and has status %s.|@
        
          Provide the @'!--allow-duplicate-names!@ flag to update the group even if another with the same title already exists.
        """.formatted(
            $pcuUpdateOptions.title.orElseThrow(),
            pcuId,
            currStatus
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--allow-duplicate-names"),
            new Hint("Get information about the existing pcu:", "${cli.name} pcu get %s".formatted($pcuUpdateOptions.title.get()))
        ));
    }

    private OutputAll handlePcuUpdated() {
        val message = "PCU group %s has been updated.".formatted(
            ctx.highlight($pcuRef)
        );

        return OutputAll.response(message, null, List.of(
            new Hint("Get more information about the updated group:", "${cli.name} pcu get %s".formatted($pcuRef))
        ));
    }

    @Override
    protected String pcuRefPrompt() {
        return "Select the PCU group to update";
    }
}
