package com.dtsx.astra.cli.gateways.user;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.sdk.org.domain.Role;
import com.dtsx.astra.sdk.org.domain.User;
import com.dtsx.astra.sdk.org.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {
    private final CliContext ctx;
    private final APIProvider apiProvider;
    private final RoleGateway roleGateway;

    @Override
    public Stream<User> findAll() {
        return ctx.log().loading("Loading users", (_) -> 
            apiProvider.astraOpsClient().users().findAll());
    }

    private Optional<User> tryFindOne(UserRef user) {
        return user.fold(
            id -> ctx.log().loading("Looking up user by ID " + ctx.highlight(user), (_) -> apiProvider.astraOpsClient().users().find(id.toString())),
            email -> ctx.log().loading("Looking up user by email " + ctx.highlight(user), (_) -> apiProvider.astraOpsClient().users().findByEmail(email))
        );
    }

    @Override
    public User findOne(UserRef user) {
        return tryFindOne(user).orElseThrow(() -> new UserNotFoundException(user.toString()));
    }

    @Override
    public CreationStatus<List<UUID>> invite(UserRef user, List<RoleRef> roles) {
        val userOpt = tryFindOne(user);

        if (userOpt.isPresent()) {
            return CreationStatus.alreadyExists(userOpt.get().getRoles().stream().map(Role::getId).map(UUID::fromString).toList());
        }

        val email = user.fold(
            _ -> findOne(user).getEmail(),
            emailStr -> emailStr
        );

        val roleIds = roles.stream()
            .map((r) -> r.fold(
                UUID::toString,
                name -> roleGateway.findOne(RoleRef.fromNameUnsafe(name)).getId()
            ))
            .toList();

        ctx.log().loading("Inviting user " + ctx.highlight(user), (_) -> {
            apiProvider.astraOpsClient().users().invite(email, roleIds.toArray(new String[0]));
            return null;
        });

        return CreationStatus.created(
            roleIds.stream().map(UUID::fromString).collect(Collectors.toList())
        );
    }

    @Override
    public DeletionStatus<Void> delete(UserRef user) {
        val userOpt = tryFindOne(user);

        if (userOpt.isEmpty()) {
            return DeletionStatus.notFound(null);
        }

        val userObj = userOpt.get();

        ctx.log().loading("Deleting user " + ctx.highlight(user), (_) -> {
            apiProvider.astraOpsClient().users().delete(userObj.getUserId());
            return null;
        });
        
        return DeletionStatus.deleted(null);
    }
}
