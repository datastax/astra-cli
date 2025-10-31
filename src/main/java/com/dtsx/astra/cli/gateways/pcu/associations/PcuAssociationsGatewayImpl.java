package com.dtsx.astra.cli.gateways.pcu.associations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DatacenterId;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupDatacenterAssociation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class PcuAssociationsGatewayImpl implements PcuAssociationsGateway {
    private final CliContext ctx;
    private final APIProvider api;
    private final PcuGateway pcuGateway;

    private Optional<PcuGroupDatacenterAssociation> tryFindOne(PcuRef group, DatacenterId dcId) {
        return ctx.log().loading("Finding association of @!%s!@ with @!%s!@".formatted(group, dcId), (_) ->
            findAll(group)
                .filter((assoc) -> assoc.getDatacenterUUID().equals(dcId.unwrap()))
                .findFirst()
        );
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
    public CreationStatus<PcuGroupDatacenterAssociation> create(PcuRef group, DatacenterId dcId) {
        val existing = tryFindOne(group, dcId);

        if (existing.isPresent()) {
            return CreationStatus.alreadyExists(existing.get());
        }

        val assoc = ctx.log().loading("Associating @!%s!@ with @!%s!@".formatted(group, dcId), (_) ->
            api.pcuGroupOpsClient(group).datacenterAssociations().associate(dcId.unwrap())
        );

        return CreationStatus.created(assoc);
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
    public DeletionStatus<PcuGroupDatacenterAssociation> transfer(UUID from, UUID to, DatacenterId dcId) {
        val assoc = ctx.log().loading("Transferring association of @!%s!@ from @!%s!@ to @!%s!@".formatted(dcId, from, to), (_) -> {
            return api.pcuGroupOpsClient(PcuRef.fromId(to)).datacenterAssociations().transfer(to.toString(), dcId.unwrap());
        });

        return DeletionStatus.deleted(assoc);
    }
}
