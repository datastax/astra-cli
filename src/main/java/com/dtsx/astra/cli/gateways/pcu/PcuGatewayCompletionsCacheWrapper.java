package com.dtsx.astra.cli.gateways.pcu;

import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupCreationRequest;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupStatusType;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.utils.MiscUtils.*;

@RequiredArgsConstructor
public class PcuGatewayCompletionsCacheWrapper implements PcuGateway {
    private final PcuGateway delegate;
    private final CompletionsCache cache; // TODO need to add detection for when to append to (or reset) the completions cache

    @Override
    public Stream<PcuGroup> findAll() {
        val pcuGroups = delegate.findAll().toList();
        cache.setCache(pcuGroups.stream().map(PcuGroup::getTitle).toList());
        return pcuGroups.stream();
    }

    @Override
    public Optional<PcuGroup> tryFindOne(PcuRef ref) {
        val res = delegate.tryFindOne(ref);

        if (res.isPresent()) {
            cache.addToCache(res.get().getTitle());
        } else {
            removeRefFromCache(ref);
        }

        return res;
    }

    @Override
    public boolean exists(PcuRef ref) {
        val exists = delegate.exists(ref);

        if (exists) {
            addRefToCache(ref);
        } else {
            removeRefFromCache(ref);
        }

        return exists;
    }

    @Override
    public void park(PcuRef ref) {
        delegate.park(ref);
    }

    @Override
    public void unpark(PcuRef ref) {
        delegate.unpark(ref);
    }

    @Override
    public Duration waitUntilPcuStatus(PcuRef ref, PcuGroupStatusType target, int timeout) {
        val duration = delegate.waitUntilPcuStatus(ref, target, timeout);
        addRefToCache(ref);
        return duration;
    }

    @Override
    public CreationStatus<PcuGroup> create(String title, PcuGroupCreationRequest req, boolean allowDuplicate) {
        val status = delegate.create(title, req, allowDuplicate);
        cache.addToCache(title);
        return status;
    }

    @Override
    public CreationStatus<PcuGroup> update(PcuRef ref, PcuGroupUpdateRequest req, boolean allowDuplicate) {
        val status = delegate.update(ref, req, allowDuplicate);
        addRefToCache(ref);
        return status;
    }

    @Override
    public DeletionStatus<PcuRef> delete(PcuRef ref) {
        val status = delegate.delete(ref);
        removeRefFromCache(ref);
        return status;
    }

    private void addRefToCache(PcuRef ref) {
        ref.fold(
            _ -> null,
            toFn((name) -> cache.update((s) -> setAdd(s, name)))
        );
    }

    private void removeRefFromCache(PcuRef ref) {
        ref.fold(
            _ -> null,
            toFn((name) -> cache.update((s) -> setDel(s, name)))
        );
    }
}
