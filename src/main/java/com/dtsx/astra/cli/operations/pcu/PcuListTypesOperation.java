package com.dtsx.astra.cli.operations.pcu;

import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.sdk.pcu.domain.PCUType;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class PcuListTypesOperation implements Operation<Stream<PCUType>> {
    private final PcuGateway pcuGateway;
    private final PcuListTypesRequest request;

    public record PcuListTypesRequest(
        Optional<CloudProvider> cloud,
        Optional<RegionName> region
    ) {}

    @Override
    public Stream<PCUType> execute() {
        return pcuGateway.listPcuTypes(request.cloud(), request.region());
    }
}
