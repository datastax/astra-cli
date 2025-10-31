package com.dtsx.astra.cli.gateways.pcu;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.internal.pcu.PcuGroupNotFoundException;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupCreationRequest;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupStatusType;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupUpdateRequest;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

public interface PcuGateway extends SomeGateway {
    Stream<PcuGroup> findAll();

    Optional<PcuGroup> tryFindOne(PcuRef ref);

    default PcuGroup findOne(PcuRef ref) {
        return tryFindOne(ref).orElseThrow(() -> new PcuGroupNotFoundException(ref));
    }

    boolean exists(PcuRef ref);

    void park(PcuRef ref);

    void unpark(PcuRef ref);

    Duration waitUntilPcuStatus(PcuRef ref, PcuGroupStatusType target, int timeout);

    CreationStatus<PcuGroup> create(String title, PcuGroupCreationRequest req, boolean allowDuplicate);

    CreationStatus<PcuGroup> update(PcuRef ref, PcuGroupUpdateRequest req, boolean allowDuplicate);

    DeletionStatus<PcuRef> delete(PcuRef ref);
}
