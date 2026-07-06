package com.dtsx.astra.cli.operations.pcu;

import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.models.PcuStatus;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.PcuUnparkOperation.PcuUnparkResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class PcuUnparkOperation implements Operation<PcuUnparkResult> {
    private final PcuGateway pcuGateway;
    private final PcuUnparkRequest request;

    public sealed interface PcuUnparkResult {}
    public record PcuUnparked(boolean neededUnparking, Duration waited) implements PcuUnparkResult {}
    public record PcuStartedUnparking() implements PcuUnparkResult {}
    public record PcuAlreadyUnparking() implements PcuUnparkResult {}
    public record PcuAlreadyUnparked() implements PcuUnparkResult {}

    public record PcuUnparkRequest(
        PcuRef pcuRef,
        LongRunningOptions lrOptions
    ) {}

    @Override
    public PcuUnparkResult execute() {
        val currentStatus = new PcuStatus(pcuGateway.findOne(request.pcuRef));

        if (currentStatus.equals(PcuStatus.UNPARKING)) {
            return handleUnparking(false, PcuAlreadyUnparking::new);
        }

        if (currentStatus.equals(PcuStatus.PARKED)) {
            pcuGateway.unpark(request.pcuRef);
            return handleUnparking(true, PcuStartedUnparking::new);
        }

        return new PcuAlreadyUnparked();
    }

    private PcuUnparkResult handleUnparking(boolean neededUnparking, Supplier<PcuUnparkResult> ifDontWait) {
        if (request.lrOptions.dontWait()) {
            return ifDontWait.get();
        }

        val waited = pcuGateway.waitUntilPcuStatus(
            request.pcuRef,
            PcuStatus.ACTIVE,
            request.lrOptions.timeout()
        );

        return new PcuUnparked(neededUnparking, waited);
    }
}
