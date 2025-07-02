package com.dtsx.astra.cli.gateways.user;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.org.domain.User;
import com.dtsx.astra.sdk.org.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {
    private final APIProvider apiProvider;

    @Override
    public Stream<User> findAll() {
        return AstraLogger.loading("Loading users", (_) -> 
            apiProvider.astraOpsClient().users().findAll());
    }

    @Override
    public Optional<User> tryFindOne(UserRef user) {
        return user.fold(
            id -> AstraLogger.loading("Looking up user by ID " + highlight(user), (_) -> 
                apiProvider.astraOpsClient().users().find(id.toString())),
            email -> AstraLogger.loading("Looking up user by email " + highlight(user), (_) -> 
                apiProvider.astraOpsClient().users().findByEmail(email))
        );
    }

    @Override
    public User findOne(UserRef user) {
        return tryFindOne(user).orElseThrow(() -> new UserNotFoundException(user.toString()));
    }

    @Override
    public void invite(UserRef user, String roleId) {
        String email = user.fold(
            _ -> findOne(user).getEmail(),
            emailStr -> emailStr
        );
        AstraLogger.loading("Inviting user " + highlight(user), (_) -> {
            apiProvider.astraOpsClient().users().invite(email, roleId);
            return null;
        });
    }

    @Override
    public DeletionStatus<Void> delete(UserRef user) {
        Optional<User> userOpt = tryFindOne(user);
        if (userOpt.isEmpty()) {
            return DeletionStatus.notFound(null);
        }
        
        User userObj = userOpt.get();
        AstraLogger.loading("Deleting user " + highlight(user), (_) -> {
            apiProvider.astraOpsClient().users().delete(userObj.getUserId());
            return null;
        });
        
        return DeletionStatus.deleted(null);
    }
}