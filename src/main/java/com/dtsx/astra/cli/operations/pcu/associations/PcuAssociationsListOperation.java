package com.dtsx.astra.cli.operations.pcu.associations;

import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.gateways.pcu.associations.PcuAssociationsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.associations.PcuAssociationsListOperation.PcuAssociationsListResult;
import com.dtsx.astra.sdk.pcu.domain.PCUGroup;
import com.dtsx.astra.sdk.pcu.domain.PCUGroupDatacenterAssociation;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class PcuAssociationsListOperation implements Operation<Stream<PcuAssociationsListResult>> {
    private final PcuGateway pcuGateway;
    private final PcuAssociationsGateway associationsGateway;
    private final PcuAssociationsListRequest request;

    public record PcuAssociationsListRequest(
        PcuRef pcuRef,
        boolean all
    ) {}

    public record PcuAssociationsListResult(
        PCUGroup pcuGroup,
        Stream<PCUGroupDatacenterAssociation> associations
    ) {}

    @Override
    public Stream<PcuAssociationsListResult> execute() {
        if (!request.all) {
            return Stream.of(
                new PcuAssociationsListResult(
                    pcuGateway.findOne(request.pcuRef),
                    associationsGateway.findAll(request.pcuRef)
                )
            );
        }

        return pcuGateway.findAll()
            .map((pcu) -> new PcuAssociationsListResult(
                pcu,
                associationsGateway.findAll(
                    PcuRef.fromId(pcu.getId())
                )
            ));
    }
}
