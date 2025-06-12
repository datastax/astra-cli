package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.config.ProfileNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ConfigDeleteOperation {
    private final AstraConfig config;

    public sealed interface DeleteConfigResult {}

    public record ProfileDoesNotExist() implements DeleteConfigResult {}
    public record ProfileDeleted() implements DeleteConfigResult {}

    public DeleteConfigResult execute(ProfileName profileName, boolean force) {
        val profileExists = config.profileExists(profileName);
        
        if (!profileExists) {
            if (!force) {
                throw new ProfileNotFoundException(profileName, "; use --force to ignore this error");
            }
            return new ProfileDoesNotExist();
        }

        config.modify((ctx) -> ctx.deleteProfile(profileName));

        return new ProfileDeleted();
    }
}
