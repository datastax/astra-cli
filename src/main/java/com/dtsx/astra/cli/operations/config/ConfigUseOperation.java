package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.exceptions.config.ProfileNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ConfigUseOperation {
    private final AstraConfig config;

    public record UseConfigRequest(
        ProfileName profileName,
        boolean force,
        boolean failIfUniqueDefault,
        Runnable assertCanOverwriteDefaultProfile
    ) {}

    public void execute(UseConfigRequest request) {
        val targetProfile = config.lookupProfile(request.profileName);
        
        if (targetProfile.isEmpty()) {
            throw new ProfileNotFoundException(request.profileName);
        }

        val profile = targetProfile.get();

        if (defaultProfileIsUnique()) {
            assertCanOverwriteDefaultProfile(request);
        }

        config.modify((ctx) -> {
            ctx.deleteProfile(ProfileName.DEFAULT);
            ctx.createProfile(ProfileName.DEFAULT, profile.token(), profile.env());
        });
    }

    private boolean defaultProfileIsUnique() {
        val defaultProfile = config.lookupProfile(ProfileName.DEFAULT);

        return defaultProfile.isPresent() && config.getValidatedProfiles().stream()
            .filter(p -> !p.name().equals(ProfileName.DEFAULT))
            .noneMatch(p -> p.token().equals(defaultProfile.get().token()) && p.env().equals(defaultProfile.get().env()));
    }

    private void assertCanOverwriteDefaultProfile(UseConfigRequest request) {
        if (request.force) {
            return;
        }

        if (request.failIfUniqueDefault) {
            throw new ExecutionCancelledException("Current default profile has unique configuration and --fail-if-unique-default was set");
        }

        request.assertCanOverwriteDefaultProfile.run();
    }
}
