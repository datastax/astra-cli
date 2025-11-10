package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigRenameOperation.ConfigRenameResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ConfigRenameOperation implements Operation<ConfigRenameResult> {
    private final AstraConfig config;
    private final RenameConfigRequest request;

    public record RenameConfigRequest(
        ProfileName oldProfileName,
        ProfileName newProfileName
    ) {}

    public sealed interface ConfigRenameResult {}
    public record ProfileRenamed(ProfileName oldProfileName, ProfileName newProfileName) implements ConfigRenameResult {}
    public record OldProfileNotFound(ProfileName oldProfileName) implements ConfigRenameResult {}
    public record NewProfileAlreadyExists(ProfileName oldProfileName, ProfileName newProfileName) implements ConfigRenameResult {}

    @Override
    public ConfigRenameResult execute() {
        val oldProfile = config.lookupProfile(request.oldProfileName);

        if (oldProfile.isEmpty()) {
            return new OldProfileNotFound(request.oldProfileName);
        }

        val newProfileExists = config.profileExists(request.newProfileName);

        if (newProfileExists) {
            return new NewProfileAlreadyExists(request.oldProfileName, request.newProfileName);
        }

        val profile = oldProfile.get();

        config.modify((ctx) -> {
            ctx.copyProfile(profile, request.newProfileName);
            ctx.deleteProfile(request.oldProfileName);
        });

        return new ProfileRenamed(request.oldProfileName, request.newProfileName);
    }
}
