package com.dtsx.astra.cli.gateways.pcu.associations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DatacenterId;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupDatacenterAssociation;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class PcuAssociationsGatewayImpl implements PcuAssociationsGateway {
    private final CliContext ctx;
    private final APIProvider api;

    private Optional<PcuGroupDatacenterAssociation> tryFindOne(PcuRef group, DatacenterId dcId) {
        return ctx.log().loading("Finding association of @!%s!@ with @!%s!@".formatted(group, dcId), (_) ->
            findAll(group)
                .filter((assoc) -> assoc.getDatacenterUUID().equals(dcId.unwrap()))
                .findFirst()
        );
    }

    @Override
    public Optional<PcuGroupDatacenterAssociation> tryFindByDatacenter(DatacenterId datacenter) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean exists(PcuRef group, DatacenterId dcId) {
        return ctx.log().loading("Checking if @!%s!@ is associated to @!%s!@".formatted(group, dcId), (_) ->
            tryFindOne(group, dcId).isPresent()
        );
    }

    @Override
    public Stream<PcuGroupDatacenterAssociation> findAll(PcuRef group) {
        return ctx.log().loading("Fetching all associations for PCU group @!%s!@".formatted(group), (_) ->
            api.pcuGroupOpsClient(group).datacenterAssociations().findAll()
        );
    }

    @Override
    public CreationStatus<Void> create(PcuRef group, DatacenterId dcId) {
        if (exists(group, dcId)) {
            return CreationStatus.alreadyExists(null);
        }

        ctx.log().loading("Associating @!%s!@ with @!%s!@".formatted(group, dcId), (_) -> {
            api.pcuGroupOpsClient(group).datacenterAssociations().associate(dcId.unwrap());
            return null;
        });

        return CreationStatus.created(null);
    }

    @Override
    public DeletionStatus<Void> delete(PcuRef group, DatacenterId dcId) {
        if (!exists(group, dcId)) {
            return DeletionStatus.notFound(null);
        }

        ctx.log().loading("Dissociating @!%s!@ from @!%s!@".formatted(group, dcId), (_) -> {
            api.pcuGroupOpsClient(group).datacenterAssociations().dissociate(dcId.unwrap());
            return null;
        });

        return DeletionStatus.deleted(null);
    }

    @Override
    public PcuGroupDatacenterAssociation transfer(UUID from, UUID to, DatacenterId dcId) {
        return ctx.log().loading("Transferring association of @!%s!@ from @!%s!@ to @!%s!@".formatted(dcId, from, to), (_) -> {
            return api.pcuGroupOpsClient(PcuRef.fromId(from)).datacenterAssociations().transfer(to.toString(), dcId.unwrap());
        });
    }
}
