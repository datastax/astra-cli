package com.dtsx.astra.cli.gateways.token;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import com.dtsx.astra.sdk.org.domain.IamToken;

import java.util.Optional;
import java.util.stream.Stream;

public interface TokenGateway extends SomeGateway {
    Stream<IamToken> findAll();

    boolean exists(String clientId);

    CreateTokenResponse create(RoleRef role, Optional<String> description);

    DeletionStatus<Void> delete(String clientId);
}
