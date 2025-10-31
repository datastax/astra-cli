package com.dtsx.astra.cli.operations.pcu;

import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupStatusType;
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
        val currentStatus = pcuGateway.findOne(request.pcuRef).getStatus();

        return switch (currentStatus) {
            case UNPARKING -> handleUnparking(false, PcuAlreadyUnparking::new);
            case PARKED -> {
                pcuGateway.unpark(request.pcuRef);
                yield handleUnparking(true, PcuStartedUnparking::new);
            }
            default -> new PcuAlreadyUnparked();
        };
    }

    private PcuUnparkResult handleUnparking(boolean neededUnparking, Supplier<PcuUnparkResult> ifDontWait) {
        if (request.lrOptions.dontWait()) {
            return ifDontWait.get();
        }

        val waited = pcuGateway.waitUntilPcuStatus(
            request.pcuRef,
            PcuGroupStatusType.ACTIVE,
            request.lrOptions.timeout()
        );

        return new PcuUnparked(neededUnparking, waited);
    }
}
