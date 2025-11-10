package com.dtsx.astra.cli.core.config;

import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Optional;

@Getter
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
@RequiredArgsConstructor
public class Profile {
    private final Optional<ProfileName> name;
    private final AstraToken token;
    private final AstraEnvironment env;
    private final Optional<ProfileName> sourceForDefault;

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
