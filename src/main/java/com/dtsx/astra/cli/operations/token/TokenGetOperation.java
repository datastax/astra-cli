package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenGetOperation implements Operation<AstraToken> {
    private final Profile profile;

    @Override
    public AstraToken execute() {
        return profile.token();
    }
}
