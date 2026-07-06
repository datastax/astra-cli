package com.dtsx.astra.cli.operations.pcu;

import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.sdk.pcu.domain.PCUGroup;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class PcuListOperation implements Operation<Stream<PCUGroup>> {
    private final PcuGateway pcuGateway;

    @Override
    public Stream<PCUGroup> execute() {
        return pcuGateway.findAll();
    }
}
