package com.dtsx.astra.cli.operations.pcu.associations;

import com.dtsx.astra.cli.core.models.PcuAssocTarget;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.pcu.associations.PcuAssociationsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.utils.DbUtils;
import com.dtsx.astra.sdk.pcu.domain.PCUGroup;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;

@RequiredArgsConstructor
public class PcuAssociationFindOperation implements Operation<Optional<PCUGroup>> {
    private final DbGateway dbGateway;
    private final PcuAssociationsGateway associationsGateway;
    private final PcuAssocTarget target;

    @Override
    public Optional<PCUGroup> execute() {
        val dcId = DbUtils.resolvePcuAssocTarget(dbGateway, target);
        return associationsGateway.tryFindByDatacenter(dcId);
    }
}
