package com.dtsx.astra.cli.operations.pcu;

import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.models.PcuStatus;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.PcuParkOperation.PcuParkResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class PcuParkOperation implements Operation<PcuParkResult> {
    private final PcuGateway pcuGateway;
    private final PcuParkRequest request;

    public sealed interface PcuParkResult {}
    public record PcuParked(boolean neededParking, Duration waited) implements PcuParkResult {}
    public record PcuStartedParking() implements PcuParkResult {}
    public record PcuAlreadyParking() implements PcuParkResult {}
    public record PcuAlreadyParked() implements PcuParkResult {}

    public record PcuParkRequest(
        PcuRef pcuRef,
        LongRunningOptions lrOptions
    ) {}

    @Override
    public PcuParkResult execute() {
        val currentStatus = new PcuStatus(pcuGateway.findOne(request.pcuRef));

        if (currentStatus.equals(PcuStatus.PARKED)) {
            return new PcuAlreadyParked();
        }

        if (currentStatus.equals(PcuStatus.PARKING)) {
            return handleParking(false, PcuAlreadyParking::new);
        }

        pcuGateway.park(request.pcuRef);
        return handleParking(true, PcuStartedParking::new);
    }

    private PcuParkResult handleParking(boolean neededParking, Supplier<PcuParkResult> ifDontWait) {
        if (request.lrOptions.dontWait()) {
            return ifDontWait.get();
        }

        val waited = pcuGateway.waitUntilPcuStatus(
            request.pcuRef,
            PcuStatus.PARKED,
            request.lrOptions.timeout()
        );

        return new PcuParked(neededParking, waited);
    }
}
