package com.dtsx.astra.cli.gateways.pcu;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.internal.pcu.PcuGroupNotFoundException;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupCreationRequest;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupStatusType;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.awaitGenericStatus;

@RequiredArgsConstructor
public class PcuGatewayImpl implements PcuGateway {
    private final CliContext ctx;
    private final APIProvider api;

    @Override
    public Stream<PcuGroup> findAll() {
        return ctx.log().loading("Fetching all PCU groups", (_) ->
            api.pcuGroupsClient().findAll()
        );
    }

    @Override
    public Optional<PcuGroup> tryFindOne(PcuRef ref) {
        return ctx.log().loading("Fetching info for PCU group " + ctx.highlight(ref), (_) ->
           api.tryResolvePcuGroup(ref)
        );
    }

    @Override
    public boolean exists(PcuRef ref) {
        return ctx.log().loading("Checking if PCU group " + ctx.highlight(ref) + " exists", (_) -> tryFindOne(ref).isPresent());
    }

    @Override
    public void park(PcuRef ref) {
        if (!exists(ref)) {
            throw new PcuGroupNotFoundException(ref);
        }

        ctx.log().loading("Parking PCU group " + ctx.highlight(ref), (_) -> {
            api.pcuGroupOpsClient(ref).park();
            return null;
        });
    }

    @Override
    public void unpark(PcuRef ref) {
        if (!exists(ref)) {
            throw new PcuGroupNotFoundException(ref);
        }

        ctx.log().loading("Parking PCU group " + ctx.highlight(ref), (_) -> {
            api.pcuGroupOpsClient(ref).unpark();
            return null;
        });
    }

    @Override
    public Duration waitUntilPcuStatus(PcuRef ref, PcuGroupStatusType target, Duration timeout) {
        return awaitGenericStatus(
            ctx,
            "PCU group %s".formatted(ctx.highlight(ref)),
            target,
            () -> findOne(ref).getStatus(),
            ctx::highlight,
            timeout
        );
    }

    @Override
    public CreationStatus<PcuGroup> create(String title, PcuGroupCreationRequest req, boolean allowDuplicate) {
        if (!allowDuplicate) {
            val existingGroup = ctx.log().loading("Checking if PCU group " + ctx.highlight(title) + " already exists", (_) -> (
                tryFindOne(PcuRef.fromTitleUnsafe(title))
            ));

            if (existingGroup.isPresent()) {
                return CreationStatus.alreadyExists(existingGroup.get());
            }
        }

        val group = ctx.log().loading("Creating PCU group " + ctx.highlight(title), (_) -> {
            return api.pcuGroupsClient().create(req);
        });

        return CreationStatus.created(group);
    }

    @Override
    public CreationStatus<PcuGroup> update(PcuRef ref, PcuGroupUpdateRequest req, boolean allowDuplicate) {
        if (!allowDuplicate && req.getTitle() != null) {
            val existingGroup = ctx.log().loading("Checking if PCU group " + ctx.highlight(req.getTitle()) + " already exists", (_) -> (
                tryFindOne(PcuRef.fromTitleUnsafe(req.getTitle()))
            ));

            if (existingGroup.isPresent()) {
                return CreationStatus.alreadyExists(existingGroup.get());
            }
        }

        if (!exists(ref)) {
            throw new PcuGroupNotFoundException(ref);
        }

        val group = ctx.log().loading("Updating PCU group " + ctx.highlight(ref), (_) -> {
            api.pcuGroupOpsClient(ref).update(req);
            return findOne(ref);
        });

        return CreationStatus.created(group);
    }

    @Override
    public DeletionStatus<PcuRef> delete(PcuRef ref) {
        if (!exists(ref)) {
            return DeletionStatus.notFound(ref);
        }

        ctx.log().loading("Deleting PCU group " + ctx.highlight(ref), (_) -> {
            api.pcuGroupOpsClient(ref).delete();
            return null;
        });

        return DeletionStatus.deleted(ref);
    }
}
