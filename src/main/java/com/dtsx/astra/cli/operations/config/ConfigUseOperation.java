package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.ConfigUseResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ConfigUseOperation implements Operation<ConfigUseResult> {
    private final AstraConfig config;
    private final UseConfigRequest request;

    public sealed interface ConfigUseResult {}
    public record ProfileSetAsDefault() implements ConfigUseResult {}
    public record ProfileNotFound() implements ConfigUseResult {}

    public record UseConfigRequest(
        ProfileName profileName
    ) {}

    @Override
    public ConfigUseResult execute() {
        val targetProfile = config.lookupProfile(request.profileName);

        if (targetProfile.isEmpty()) {
            return new ProfileNotFound();
        }

        val profile = targetProfile.get();

        config.modify((ctx) -> {
            ctx.deleteProfile(ProfileName.DEFAULT);
            ctx.createProfile(ProfileName.DEFAULT, profile.token(), profile.env());
        });

        return new ProfileSetAsDefault();
    }
}
