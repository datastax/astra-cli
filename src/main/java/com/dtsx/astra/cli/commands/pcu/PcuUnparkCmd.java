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
import com.dtsx.astra.cli.operations.pcu.PcuUnparkOperation;
import com.dtsx.astra.cli.operations.pcu.PcuUnparkOperation.*;
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

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.ExitCode.PCU_GROUP_NOT_FOUND;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "unpark",
    description = "Unpark a parked PCU group"
)
@Example(
    comment = "Unpark a parked PCU group",
    command = "${cli.name} pcu unpark my_pcu"
)
public class PcuUnparkCmd extends AbstractPromptForPcuCmd<PcuUnparkResult> implements WithSetTimeout {
    @Option(
        names = LR_OPTS_TIMEOUT_NAME,
        description = LR_OPTS_TIMEOUT_DESC,
        defaultValue = "30m"
    )
    public void setTimeout(Duration timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Mixin
    protected LongRunningOptionsMixin lrMixin;

    @Override
    protected final OutputAll execute(Supplier<PcuUnparkResult> result) {
        return switch (result.get()) {
            case PcuUnparked(var neededUnparking, var waited) -> handlePcuUnparked(neededUnparking, waited);
            case PcuAlreadyUnparked() -> handlePcuAlreadyUnparked();
            case PcuAlreadyUnparking() -> handlePcuAlreadyUnparking();
            case PcuStartedUnparking() -> handlePcuStartedUnparking();
        };
    }

    private OutputAll handlePcuUnparked(boolean neededUnparking, Duration waited) {
        val message = "PCU group @!%s!@ is now unparked after waiting %d seconds.".formatted(
            $pcuRef,
            waited.getSeconds()
        );

        val data = mkData(neededUnparking, PcuGroupStatusType.ACTIVE, waited);

        return OutputAll.response(message, data);
    }

    private OutputAll handlePcuAlreadyUnparked() {
        val message = "PCU group @!%s!@ was already unparked; no action was required.".formatted($pcuRef);

        val data = mkData(false, PcuGroupStatusType.ACTIVE, Duration.ZERO);

        return OutputAll.response(message, data);
    }

    private OutputAll handlePcuAlreadyUnparking() {
        val message = "PCU group @!%s!@ was already unparking; no action was required.".formatted($pcuRef);

        val data = mkData(false, PcuGroupStatusType.UNPARKING, Duration.ZERO);

        return OutputAll.response(message, data, List.of(
            new Hint("Poll the group's status:", "${cli.name} pcu status %s".formatted($pcuRef))
        ));
    }

    private OutputAll handlePcuStartedUnparking() {
        val message = "PCU group @!%s!@ is currently unparking".formatted($pcuRef);

        val data = mkData(true, PcuGroupStatusType.UNPARKING, Duration.ZERO);

        return OutputAll.response(message, data, List.of(
            new Hint("Poll the group's status:", "${cli.name} pcu status %s".formatted($pcuRef))
        ));
    }

    @Override
    protected Operation<PcuUnparkResult> mkOperation() {
        return new PcuUnparkOperation(pcuGateway, new PcuUnparkRequest(
            $pcuRef,
            lrMixin.options(ctx)
        ));
    }

    private LinkedHashMap<String, Object> mkData(Boolean neededUnparking, PcuGroupStatusType currentStatus, @Nullable Duration waitedDuration) {
        return sequencedMapOf(
            "neededUnparking", neededUnparking,
            "currentStatus", currentStatus,
            "waitedSeconds", Optional.ofNullable(waitedDuration).map(Duration::getSeconds)
        );
    }

    @Override
    protected String pcuRefPrompt() {
        return "Select the PCU group to unpark";
    }

    @Override
    protected NEList<PcuGroup> modifyPcusPromptList(NEList<PcuGroup> pcus) {
        return NEList.parse(
            pcus.stream().filter((pcu) -> pcu.getStatus() == PcuGroupStatusType.UNPARKING || pcu.getStatus() == PcuGroupStatusType.PARKED).toList()
        ).orElseThrow(
            () -> new AstraCliException(PCU_GROUP_NOT_FOUND, "@|bold,red No parked PCU groups found to select from|@")
        );
    }
}
