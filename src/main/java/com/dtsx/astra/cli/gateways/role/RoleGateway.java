package com.dtsx.astra.cli.gateways.role;

import com.dtsx.astra.cli.core.models.Token;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.org.domain.Role;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface RoleGateway {
    static RoleGateway mkDefault(Token token, AstraEnvironment env) {
        return new RoleGatewayImpl(APIProvider.mkDefault(token, env));
    }

    Stream<Role> findAll();
    Optional<Role> tryFindOne(String role);
    Role findOne(String role);
}
