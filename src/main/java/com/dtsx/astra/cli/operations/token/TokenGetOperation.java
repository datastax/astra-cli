package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenGetOperation implements Operation<AstraToken> {
    private final TokenGateway tokenGateway;
    private final TokenGetRequest request;

    public record TokenGetRequest(Profile profile, boolean validate) {}

    @Override
    public AstraToken execute() {
        if (request.validate) {
            tokenGateway.validate(request.profile.token());
        }
        return request.profile.token();
    }
}
