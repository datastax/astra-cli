package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigDeleteOperation.ConfigDeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ConfigDeleteOperation implements Operation<ConfigDeleteResult> {
    private final AstraConfig config;
    private final CreateDeleteRequest request;

    public record CreateDeleteRequest(
        ProfileName profileName,
        boolean ifNotExists
    ) {}

    public sealed interface ConfigDeleteResult {}
    public record ProfileDoesNotExist() implements ConfigDeleteResult {}
    public record ProfileIllegallyDoesNotExist() implements ConfigDeleteResult {}
    public record ProfileDeleted() implements ConfigDeleteResult {}

    @Override
    public ConfigDeleteResult execute() {
        val profileExists = config.profileExists(request.profileName);

        if (!profileExists) {
            if (request.ifNotExists) {
                return new ProfileDoesNotExist();
            }
            return new ProfileIllegallyDoesNotExist();
        }

        config.modify((ctx) -> ctx.deleteProfile(request.profileName));

        return new ProfileDeleted();
    }
}
