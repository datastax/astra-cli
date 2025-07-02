package com.dtsx.astra.cli.gateways.user;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.Token;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.org.domain.User;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.Optional;
import java.util.stream.Stream;

public interface UserGateway {
    static UserGateway mkDefault(Token token, AstraEnvironment env) {
        return new UserGatewayImpl(APIProvider.mkDefault(token, env));
    }

    Stream<User> findAll();
    Optional<User> tryFindOne(UserRef user);
    User findOne(UserRef user);
    void invite(UserRef user, String roleId);
    DeletionStatus<Void> delete(UserRef user);
}