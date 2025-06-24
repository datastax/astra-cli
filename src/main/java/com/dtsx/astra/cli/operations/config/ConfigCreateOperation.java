package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.exceptions.misc.InvalidTokenException;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigCreateOperation.ConfigCreateResult;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ConfigCreateOperation implements Operation<ConfigCreateResult> {
    private final AstraConfig config;
    private final CreateConfigRequest request;

    public record CreateConfigRequest(
        Optional<ProfileName> profileName,
        String token,
        AstraEnvironment env,
        boolean force,
        boolean failIfExists,
        Runnable assertShouldSetDefaultProfile,
        Consumer<ProfileName> assertCanOverwriteProfile
    ) {}

    public sealed interface ConfigCreateResult { ProfileName profileName(); }
    public record ProfileWasCreated(ProfileName profileName) implements ConfigCreateResult {}
    public record ProfileWasOverwritten(ProfileName profileName) implements ConfigCreateResult {}

    @Override
    public ConfigCreateResult execute() {
        val org = validateTokenAndFetchOrg(request.token, request.env);
        val profileName = mkProfileName(org, request);

        val profileExists = config.profileExists(profileName);

        if (profileExists) {
            assertCanOverwriteProfile(profileName, request);
        }

        config.modify((ctx) -> {
            ctx.deleteProfile(profileName);
            ctx.createProfile(profileName, request.token, request.env);
        });

        return (profileExists)
            ? new ProfileWasOverwritten(profileName)
            : new ProfileWasCreated(profileName);
    }

    private Organization validateTokenAndFetchOrg(String token, AstraEnvironment env) {
        try {
            return AstraLogger.loading("Validating your Astra token", (_) -> (
                new AstraOpsClient(token, env).getOrganization()
            ));
        } catch (Exception e) {
            throw new InvalidTokenException("Error validating your astra token" + ((env != AstraEnvironment.PROD) ? "; make sure token targets the proper environment (%s)" : ""));
        }
    }

    private ProfileName mkProfileName(Organization org, CreateConfigRequest request) {
        val profileName = request.profileName.orElse(ProfileName.mkUnsafe(org.getName()));

        if (profileName.isDefault()) {
            assertShouldSetDefaultProfile(request);
        }

        return profileName;
    }

    private void assertShouldSetDefaultProfile(CreateConfigRequest request) {
        if (!request.force) {
            request.assertShouldSetDefaultProfile.run();
        }
    }

    private void assertCanOverwriteProfile(ProfileName profileName, CreateConfigRequest request) {
        if (request.force) {
            return;
        }

        if (request.failIfExists) {
            throw new ExecutionCancelledException("Operation canceled because profile %s already exists, and --fail-if-exists was set.".formatted(profileName));
        }

        request.assertCanOverwriteProfile.accept(profileName);
    }
}
