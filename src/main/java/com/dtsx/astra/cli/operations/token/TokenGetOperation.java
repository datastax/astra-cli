package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.cli.core.models.Token;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenGetOperation implements Operation<Token> {
    private final Profile profile;

    @Override
    public Token execute() {
        return profile.token();
    }
}