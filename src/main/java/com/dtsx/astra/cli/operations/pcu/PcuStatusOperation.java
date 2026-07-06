package com.dtsx.astra.cli.operations.pcu;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.models.PcuStatus;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PcuStatusOperation implements Operation<PcuStatus> {
    private final CliContext ctx;
    private final PcuGateway pcuGateway;
    private final PcuStatusRequest request;

    public record PcuStatusRequest(PcuRef pcuRef) {}

    @Override
    public PcuStatus execute() {
        return ctx.log().loading("Fetching status for PCU group " + ctx.highlight(request.pcuRef), (_) -> {
            return new PcuStatus(pcuGateway.findOne(request.pcuRef).getStatus());
        });
    }
}
