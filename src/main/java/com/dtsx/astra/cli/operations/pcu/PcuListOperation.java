package com.dtsx.astra.cli.operations.pcu;

import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class PcuListOperation implements Operation<Stream<PcuGroup>> {
    private final PcuGateway pcuGateway;

    @Override
    public Stream<PcuGroup> execute() {
        return pcuGateway.findAll();
    }
}
