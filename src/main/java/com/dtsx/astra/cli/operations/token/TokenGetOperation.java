package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenGetOperation implements Operation<AstraToken> {
    private final CliContext ctx;
    private final Profile profile;
    private final TokenGateway tokenGateway;
    private final TokenGetRequest request;

    public record TokenGetRequest(boolean validate) {}

    @Override
    public AstraToken execute() {
        if (request.validate) {
            tokenGateway.validate(profile.token());
        }
        return profile.token();
    }
}
