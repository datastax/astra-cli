package com.dtsx.astra.cli.gateways.role;

import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.org.domain.Role;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.Optional;

public interface RoleGateway {
    static RoleGateway mkDefault(String token, AstraEnvironment env) {
        return new RoleGatewayImpl(APIProvider.mkDefault(token, env));
    }

    Optional<Role> tryFindOne(String role);
    Role findOne(String role);
}
