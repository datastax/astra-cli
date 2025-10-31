package com.dtsx.astra.cli.gateways.pcu.associations;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DatacenterId;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupDatacenterAssociation;

import java.util.UUID;
import java.util.stream.Stream;

public interface PcuAssociationsGateway {
    boolean exists(PcuRef group, DatacenterId datacenter);

    Stream<PcuGroupDatacenterAssociation> findAll(PcuRef group);

    CreationStatus<PcuGroupDatacenterAssociation> create(PcuRef group, DatacenterId datacenter);

    DeletionStatus<PcuGroupDatacenterAssociation> transfer(UUID from, UUID to, DatacenterId datacenter);

    DeletionStatus<Void> delete(PcuRef group, DatacenterId datacenter);
}
