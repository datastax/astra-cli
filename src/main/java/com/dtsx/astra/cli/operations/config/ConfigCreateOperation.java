package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.exceptions.misc.InvalidTokenException;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ConfigCreateOperation {
    private final AstraConfig config;

    public record CreateConfigRequest(
        Optional<ProfileName> profileName,
        String token,
        AstraEnvironment env,
        boolean force,
        boolean failIfExists,
        Runnable assertShouldSetDefaultProfile,
        Consumer<ProfileName> assertCanOverwriteProfile
    ) {}
    
    public record ProfileCreatedResult(ProfileName profileName, boolean profileWasOverwritten) {}

    public ProfileCreatedResult execute(CreateConfigRequest request) {
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

        return new ProfileCreatedResult(profileName, profileExists);
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

        if (profileName.equals(ProfileName.DEFAULT)) {
            assertShouldSetDefaultProfile(request);
        }

        return profileName;
    }

    private boolean profileExists(ProfileName profileName) {
        return config.lookupProfile(profileName).isPresent();
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
