package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.WithSetTimeout;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupStatusType;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.PcuParkOperation;
import com.dtsx.astra.cli.operations.pcu.PcuParkOperation.*;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DB_ACTIVE_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.ExitCode.PCU_GROUP_NOT_FOUND;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "park",
    description = "Park an existing PCU group"
)
@Example(
    comment = "Park an existing PCU group",
    command = "${cli.name} pcu park my_pcu"
)
public class PcuParkCmd extends AbstractPromptForPcuCmd<PcuParkResult> implements WithSetTimeout {
    @Option(
        names = LR_OPTS_TIMEOUT_NAME,
        description = LR_OPTS_TIMEOUT_DB_ACTIVE_DESC,
        defaultValue = "30m"
    )
    public void setTimeout(Duration timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Mixin
    protected LongRunningOptionsMixin lrMixin;

    @Override
    protected final OutputAll execute(Supplier<PcuParkResult> result) {
        return switch (result.get()) {
            case PcuParked(var neededParking, var waited) -> handlePcuParked(neededParking, waited);
            case PcuAlreadyParked() -> handlePcuAlreadyParked();
            case PcuAlreadyParking() -> handlePcuAlreadyParking();
            case PcuStartedParking() -> handlePcuStartedParking();
        };
    }

    private OutputAll handlePcuParked(boolean neededParking, Duration waited) {
        val message = "PCU group @!%s!@ is now parked after waiting %d seconds.".formatted(
            $pcuRef,
            waited.getSeconds()
        );

        val data = mkData(neededParking, PcuGroupStatusType.PARKED, waited);

        return OutputAll.response(message, data);
    }

    private OutputAll handlePcuAlreadyParked() {
        val message = "PCU group @!%s!@ was already parked; no action was required.".formatted($pcuRef);

        val data = mkData(false, PcuGroupStatusType.PARKED, Duration.ZERO);

        return OutputAll.response(message, data);
    }

    private OutputAll handlePcuAlreadyParking() {
        val message = "PCU group @!%s!@ was already parking; no action was required.".formatted($pcuRef);

        val data = mkData(false, PcuGroupStatusType.PARKING, Duration.ZERO);

        return OutputAll.response(message, data, List.of(
            new Hint("Poll the group's status:", "${cli.name} pcu status %s".formatted($pcuRef))
        ));
    }

    private OutputAll handlePcuStartedParking() {
        val message = "PCU group @!%s!@ is currently parking".formatted($pcuRef);

        val data = mkData(true, PcuGroupStatusType.PARKING, Duration.ZERO);

        return OutputAll.response(message, data, List.of(
            new Hint("Poll the group's status:", "${cli.name} pcu status %s".formatted($pcuRef))
        ));
    }

    @Override
    protected Operation<PcuParkResult> mkOperation() {
        return new PcuParkOperation(pcuGateway, new PcuParkRequest(
            $pcuRef,
            lrMixin.options(ctx)
        ));
    }

    private LinkedHashMap<String, Object> mkData(Boolean neededParking, PcuGroupStatusType currentStatus, @Nullable Duration waitedDuration) {
        return sequencedMapOf(
            "neededParking", neededParking,
            "currentStatus", currentStatus,
            "waitedSeconds", Optional.ofNullable(waitedDuration).map(Duration::getSeconds)
        );
    }

    @Override
    protected String pcuRefPrompt() {
        return "Select the PCU group to park";
    }

    @Override
    protected NEList<PcuGroup> modifyPcusPromptList(NEList<PcuGroup> pcus) {
        return NEList.parse(
            pcus.stream().filter((pcu) -> pcu.getStatus() != PcuGroupStatusType.PARKING && pcu.getStatus() != PcuGroupStatusType.PARKED).toList()
        ).orElseThrow(
            () -> new AstraCliException(PCU_GROUP_NOT_FOUND, "@|bold,red No unparked PCU groups found to select from|@")
        );
    }
}
