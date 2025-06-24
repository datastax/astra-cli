package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenGetOperation implements Operation<String> {
    private final Profile profile;

    @Override
    public String execute() {
        return profile.token();
    }
}