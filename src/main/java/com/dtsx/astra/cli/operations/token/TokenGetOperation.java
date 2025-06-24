package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.config.AstraConfig.Profile;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenGetOperation {
    private final Profile profile;

    public String execute() {
        return profile.token();
    }
}