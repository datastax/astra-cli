package com.dtsx.astra.cli.core.config;

import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.Optional;

public record Profile(Optional<ProfileName> name, AstraToken token, AstraEnvironment env) {
    public boolean isDefault() {
        return name.map(ProfileName::isDefault).orElse(false);
    }

    public boolean isReconstructedFromCreds() {
        return name.isEmpty();
    }

    public ProfileName nameOrDefault() {
        return name.orElse(ProfileName.mkUnsafe("<args_provided>"));
    }
}
