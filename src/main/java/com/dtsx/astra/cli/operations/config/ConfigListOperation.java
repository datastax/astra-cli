package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigListOperation.ListConfigResult;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

@RequiredArgsConstructor
public class ConfigListOperation implements Operation<ListConfigResult> {
    private final AstraConfig config;

    public record ListConfigResult(
        List<ProfileInfo> profiles
    ) {}

    public record ProfileInfo(
        String name,
        AstraToken token,
        AstraEnvironment env,
        boolean isInUse
    ) {}

    @Override
    public ListConfigResult execute() {
        val defaultToken = config
            .lookupProfile(ProfileName.DEFAULT)
            .map(Profile::token)
            .orElse(null);

        val profiles = config.getValidatedProfiles().stream()
            .filter(p -> !p.isDefault())
            .map(p -> new ProfileInfo(
                p.nameOrDefault().unwrap(),
                p.token(),
                p.env(),
                p.token().equals(defaultToken)
            ))
            .toList();

        return new ListConfigResult(profiles);
    }
}
