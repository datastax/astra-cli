package com.dtsx.astra.cli.testlib.doubles.gateways;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import com.dtsx.astra.sdk.org.domain.IamToken;

import java.util.Optional;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.testlib.doubles.Utils.methodIllegallyCalled;

public class TokenGatewayStub implements TokenGateway {
    @Override
    public Stream<IamToken> findAll() {
        return methodIllegallyCalled();
    }

    @Override
    public boolean exists(String clientId) {
        return methodIllegallyCalled();
    }

    @Override
    public CreateTokenResponse create(RoleRef role, Optional<String> description) {
        return methodIllegallyCalled();
    }

    @Override
    public DeletionStatus<Void> delete(String clientId) {
        return methodIllegallyCalled();
    }
}
