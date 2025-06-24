package com.dtsx.astra.cli.gateways.token;

import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import com.dtsx.astra.sdk.org.domain.IamToken;
import com.dtsx.astra.sdk.org.domain.Role;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;
import java.util.Optional;

public interface TokenGateway {
    static TokenGateway mkDefault(String token, AstraEnvironment env) {
        return new TokenGatewayImpl(APIProvider.mkDefault(token, env));
    }

    List<IamToken> findAll();
    Optional<IamToken> tryFindOne(String clientId);
    boolean exists(String clientId);
    CreateTokenResponse create(Role role);
    void delete(String clientId);
}
