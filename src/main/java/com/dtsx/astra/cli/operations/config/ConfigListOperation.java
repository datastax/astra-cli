package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

@RequiredArgsConstructor
public class ConfigListOperation {
    private final AstraConfig config;

    public record ListConfigResult(
        List<ProfileInfo> profiles
    ) {}

    public record ProfileInfo(
        String name,
        String token,
        AstraEnvironment env,
        boolean isInUse
    ) {}

    public ListConfigResult execute() {
        val defaultToken = config
            .lookupProfile(ProfileName.DEFAULT)
            .map(Profile::token)
            .orElse(null);

        val profiles = config.getValidatedProfiles().stream()
            .filter(p -> !p.isDefault())
            .map(p -> new ProfileInfo(
                p.name().unwrap(),
                p.token(),
                p.env(),
                p.token().equals(defaultToken)
            ))
            .toList();

        return new ListConfigResult(profiles);
    }
}
