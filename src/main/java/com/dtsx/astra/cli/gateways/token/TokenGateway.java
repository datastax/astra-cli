package com.dtsx.astra.cli.gateways.token;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import com.dtsx.astra.sdk.org.domain.IamToken;
import com.dtsx.astra.sdk.org.domain.Role;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.stream.Stream;

public interface TokenGateway {
    static TokenGateway mkDefault(AstraToken token, AstraEnvironment env) {
        return new TokenGatewayImpl(APIProvider.mkDefault(token, env));
    }

    Stream<IamToken> findAll();

    boolean exists(String clientId);

    CreateTokenResponse create(Role role);

    DeletionStatus<Void> delete(String clientId);
}
