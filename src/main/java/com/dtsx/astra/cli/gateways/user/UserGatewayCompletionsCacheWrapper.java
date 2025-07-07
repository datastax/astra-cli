package com.dtsx.astra.cli.gateways.user;

import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.sdk.org.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.utils.MiscUtils.*;

@RequiredArgsConstructor
public class UserGatewayCompletionsCacheWrapper implements UserGateway {
    private final UserGateway delegate;
    private final CompletionsCache cache;

    @Override
    public Stream<User> findAll() {
        val users = delegate.findAll().toList();
        cache.setCache(users.stream().map(User::getEmail).toList());
        return users.stream();
    }

    @Override
    public User findOne(UserRef user) {
        val foundUser = delegate.findOne(user);
        cache.addToCache(foundUser.getEmail());
        return foundUser;
    }

    @Override
    public CreationStatus<List<UUID>> invite(UserRef user, List<RoleRef> roles) {
        val roleIds = delegate.invite(user, roles);
        addRefToCache(user);
        return roleIds;
    }

    @Override
    public DeletionStatus<Void> delete(UserRef user) {
        val status = delegate.delete(user);
        removeRefFromCache(user);
        return status;
    }

    private void addRefToCache(UserRef ref) {
        ref.fold(
            _ -> null,
            toFn((email) -> cache.update((s) -> setAdd(s, email)))
        );
    }

    private void removeRefFromCache(UserRef ref) {
        ref.fold(
            _ -> null,
            toFn((email) -> cache.update((s) -> setDel(s, email)))
        );
    }
}
