package com.dtsx.astra.cli.gateways.pcu.associations;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DatacenterId;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.pcu.domain.PCUGroupDatacenterAssociation;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface PcuAssociationsGateway extends SomeGateway {
    boolean exists(PcuRef group, DatacenterId datacenter);

    Optional<PCUGroupDatacenterAssociation> tryFindByDatacenter(DatacenterId datacenter);

    Stream<PCUGroupDatacenterAssociation> findAll(PcuRef group);

    CreationStatus<Void> create(PcuRef group, DatacenterId datacenter);

    PCUGroupDatacenterAssociation transfer(UUID from, UUID to, DatacenterId datacenter);

    DeletionStatus<Void> delete(PcuRef group, DatacenterId datacenter);
}
