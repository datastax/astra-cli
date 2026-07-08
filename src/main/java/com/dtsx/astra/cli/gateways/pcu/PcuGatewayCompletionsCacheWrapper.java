package com.dtsx.astra.cli.gateways.pcu;

import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.*;
import com.dtsx.astra.sdk.pcu.domain.PCUGroup;
import com.dtsx.astra.sdk.pcu.domain.PCUGroupCreationRequest;
import com.dtsx.astra.sdk.pcu.domain.PCUGroupUpdateRequest;
import com.dtsx.astra.sdk.pcu.domain.PCUType;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.utils.MiscUtils.toFn;

@RequiredArgsConstructor
public class PcuGatewayCompletionsCacheWrapper implements PcuGateway {
    private final PcuGateway delegate;
    private final CompletionsCache cache;

    @Override
    public Stream<PCUGroup> findAll() {
        val pcuGroups = delegate.findAll().toList();
        cache.setCache(pcuGroups.stream().map(PCUGroup::getTitle).toList());
        return pcuGroups.stream();
    }

    @Override
    public Stream<PCUType> findPcuTypes(Optional<CloudProvider> provider, Optional<RegionRef> region) {
        return delegate.findPcuTypes(provider, region);
    }

    @Override
    public Optional<PCUGroup> tryFindOne(PcuRef ref) {
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
    public Duration waitUntilPcuStatus(PcuRef ref, PcuStatus target, Duration timeout) {
        val duration = delegate.waitUntilPcuStatus(ref, target, timeout);
        addRefToCache(ref);
        return duration;
    }

    @Override
    public CreationStatus<PCUGroup> create(String title, PCUGroupCreationRequest req, boolean allowDuplicate) {
        val status = delegate.create(title, req, allowDuplicate);
        cache.addToCache(title);
        return status;
    }

    @Override
    public CreationStatus<PCUGroup> update(PcuRef ref, PCUGroupUpdateRequest req, boolean allowDuplicate) {
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
            toFn(cache::addToCache)
        );
    }

    private void removeRefFromCache(PcuRef ref) {
        ref.fold(
            _ -> null,
            toFn(cache::removeFromCache)
        );
    }
}
