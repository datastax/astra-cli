package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class TokenCreateOperation implements Operation<CreateTokenResponse> {
    private final TokenGateway tokenGateway;
    private final RoleGateway roleGateway;
    private final TokenCreateRequest request;

    public record TokenCreateRequest(RoleRef role) {}

    @Override
    public CreateTokenResponse execute() {
        val role = roleGateway.findOne(request.role);
        return tokenGateway.create(role);
    }
}
