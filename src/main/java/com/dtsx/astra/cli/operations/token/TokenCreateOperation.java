package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class TokenCreateOperation implements Operation<CreateTokenResponse> {
    private final TokenGateway tokenGateway;
    private final TokenCreateRequest request;

    public record TokenCreateRequest(
        NEList<RoleRef> roles,
        Optional<String> description,
        Optional<UUID> orgId,
        Optional<Instant> expiration
    ) {}

    @Override
    public CreateTokenResponse execute() {
        return tokenGateway.create(request.roles, request.description, request.orgId, request.expiration);
    }
}
