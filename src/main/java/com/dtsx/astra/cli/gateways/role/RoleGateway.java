package com.dtsx.astra.cli.gateways.role;

import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.org.domain.Role;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.Optional;
import java.util.stream.Stream;

public interface RoleGateway {
    static RoleGateway mkDefault(AstraToken token, AstraEnvironment env, CompletionsCache roleCompletionsCache) {
        return new RoleGatewayCompletionsCacheWrapper(new RoleGatewayImpl(APIProvider.mkDefault(token, env)), roleCompletionsCache);
    }

    Stream<Role> findAll();

    Optional<Role> tryFindOne(RoleRef role);

    Role findOne(RoleRef role);
}
