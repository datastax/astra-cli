package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class TokenCreateOperation implements Operation<CreateTokenResponse> {
    private final TokenGateway tokenGateway;
    private final TokenCreateRequest request;

    public record TokenCreateRequest(
        RoleRef role,
        Optional<String> description
    ) {}

    @Override
    public CreateTokenResponse execute() {
        return tokenGateway.create(request.role, request.description);
    }
}
