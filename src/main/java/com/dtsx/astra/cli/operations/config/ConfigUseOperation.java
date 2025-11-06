package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.ConfigUseResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ConfigUseOperation implements Operation<ConfigUseResult> {
    private final AstraConfig config;
    private final UseConfigRequest request;

    public sealed interface ConfigUseResult {}
    public record ProfileSetAsDefault(ProfileName profileName) implements ConfigUseResult {}
    public record ProfileNotFound(ProfileName profileName) implements ConfigUseResult {}

    public record UseConfigRequest(
        ProfileName profileName
    ) {}

    @Override
    public ConfigUseResult execute() {
        val retrievedProfile = config.lookupProfile(request.profileName);

        if (retrievedProfile.isEmpty()) {
            return new ProfileNotFound(request.profileName);
        }

        val profile = retrievedProfile.get();

        config.modify((ctx) -> {
            ctx.copyProfile(profile, ProfileName.DEFAULT);
        });

        return new ProfileSetAsDefault(request.profileName);
    }
}
