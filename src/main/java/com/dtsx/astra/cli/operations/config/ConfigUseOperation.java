package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.ConfigUseResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class ConfigUseOperation implements Operation<ConfigUseResult> {
    private final AstraConfig config;
    private final UseConfigRequest request;

    public sealed interface ConfigUseResult {}
    public record ProfileSetAsDefault(ProfileName profileName) implements ConfigUseResult {}
    public record ProfileNotFound(ProfileName profileName) implements ConfigUseResult {}

    public record UseConfigRequest(
        Optional<ProfileName> profileName,
        BiFunction<Optional<Profile>, NEList<Profile>, Profile> promptForProfile
    ) {}

    @Override
    public ConfigUseResult execute() {
        val targetProfileName = request.profileName.orElseGet(() -> {
            val profiles = NEList.parse(config.getValidatedProfiles().stream().filter(p -> !p.isDefault()).toList());

            if (profiles.isEmpty()) {
                throw new AstraCliException("""
                  @|bold,red No profiles found to select from|@
                """);
            }

            val defaultProfile = config.lookupProfile(ProfileName.DEFAULT);

            return request.promptForProfile.apply(defaultProfile, profiles.get()).nameOrDefault();
        });

        val retrievedProfile = config.lookupProfile(targetProfileName);

        if (retrievedProfile.isEmpty()) {
            return new ProfileNotFound(targetProfileName);
        }

        val profile = retrievedProfile.get();

        config.modify((ctx) -> {
            ctx.copyProfile(profile, ProfileName.DEFAULT);
        });

        return new ProfileSetAsDefault(targetProfileName);
    }
}
