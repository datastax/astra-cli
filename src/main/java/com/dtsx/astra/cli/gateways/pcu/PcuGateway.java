package com.dtsx.astra.cli.gateways.pcu;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.internal.pcu.PcuGroupNotFoundException;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.models.PcuStatus;
import com.dtsx.astra.cli.core.models.RegionRef;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.pcu.domain.PCUGroup;
import com.dtsx.astra.sdk.pcu.domain.PCUGroupCreationRequest;
import com.dtsx.astra.sdk.pcu.domain.PCUGroupUpdateRequest;
import com.dtsx.astra.sdk.pcu.domain.PCUType;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

public interface PcuGateway extends SomeGateway {
    Stream<PCUGroup> findAll();

    Stream<PCUType> findPcuTypes(Optional<CloudProvider> provider, Optional<RegionRef> region);

    Optional<PCUGroup> tryFindOne(PcuRef ref);

    default PCUGroup findOne(PcuRef ref) {
        return tryFindOne(ref).orElseThrow(() -> new PcuGroupNotFoundException(ref));
    }

    boolean exists(PcuRef ref);

    void park(PcuRef ref);

    void unpark(PcuRef ref);

    Duration waitUntilPcuStatus(PcuRef ref, PcuStatus target, Duration timeout);

    CreationStatus<PCUGroup> create(String title, PCUGroupCreationRequest req, boolean allowDuplicate);

    CreationStatus<PCUGroup> update(PcuRef ref, PCUGroupUpdateRequest req, boolean allowDuplicate);

    DeletionStatus<PcuRef> delete(PcuRef ref);
}
