package com.dtsx.astra.cli.gateways.role;

import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.sdk.org.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.utils.MiscUtils.toFn;

@RequiredArgsConstructor
public class RoleGatewayCompletionsCacheWrapper implements RoleGateway {
    private final RoleGateway delegate;
    private final CompletionsCache cache;

    @Override
    public Stream<Role> findAll() {
        val roles = delegate.findAll().toList();
        cache.setCache(roles.stream().map(Role::getName).toList());
        return roles.stream();
    }

    @Override
    public Stream<Role> findAll(List<RoleRef> refs) {
        val roles = delegate.findAll(refs).toList();
        roles.forEach(role -> cache.addToCache(role.getName()));
        return roles.stream();
    }

    @Override
    public Optional<Role> tryFindOne(RoleRef ref) {
        val res = delegate.tryFindOne(ref);

        if (res.isPresent()) {
            cache.addToCache(res.get().getName());
        } else {
            removeRefFromCache(ref);
        }

        return res;
    }

    @Override
    public Map<UUID, String> findNames(Set<UUID> ids) {
        val res = delegate.findNames(ids);

        for (val name : res.values()) {
            cache.addToCache(name);
        }

        return res;
    }

    private void removeRefFromCache(RoleRef ref) {
        ref.fold(
            _ -> null,
            toFn(cache::removeFromCache)
        );
    }
}
