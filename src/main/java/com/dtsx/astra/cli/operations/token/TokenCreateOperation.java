package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class TokenCreateOperation {
    private final TokenGateway tokenGateway;
    private final RoleGateway roleGateway;

    public CreateTokenResponse execute(String roleInput) {
        val role = roleGateway.findOne(roleInput);
        return tokenGateway.create(role);
    }
}
