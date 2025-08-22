package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigCreateOperation.ConfigCreateResult;
import com.dtsx.astra.sdk.exception.AuthenticationException;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ConfigCreateOperation implements Operation<ConfigCreateResult> {
    private final AstraConfig config;
    private final OrgGateway orgGateway;
    private final CreateConfigRequest request;

    public record CreateConfigRequest(
        Optional<ProfileName> profileName,
        AstraToken token,
        AstraEnvironment env,
        Optional<Boolean> overwrite,
        boolean setDefault,
        Consumer<ProfileName> assertCanOverwriteProfile
    ) {}

    public sealed interface ConfigCreateResult {}
    public record ProfileCreated(ProfileName profileName, boolean overwritten, boolean isDefault) implements ConfigCreateResult {}
    public record ProfileIllegallyExists(ProfileName profileName) implements ConfigCreateResult {}
    public record ViolatedFailIfExists() implements ConfigCreateResult {}
    public record InvalidToken() implements ConfigCreateResult {}

    @Override
    public ConfigCreateResult execute() {
        val org = validateTokenAndFetchOrg(orgGateway);

        if (org.isEmpty()) {
            return new InvalidToken();
        }

        val profileName = resolveProfileName(org.get(), request);

        if (profileName.isDefault()) {
            return new ViolatedFailIfExists();
        }

        val profileExists = config.profileExists(profileName);

        if (profileExists) {
            val res = assertCanOverwriteProfile(profileName, request);

            if (res.isPresent()) {
                return res.get();
            }
        }

        config.modify((ctx) -> {
            ctx.deleteProfile(profileName);
            ctx.createProfile(profileName, request.token, request.env);


            if (request.setDefault) {
                ctx.deleteProfile(ProfileName.DEFAULT);
                ctx.createProfile(ProfileName.DEFAULT, request.token, request.env);
            }
        });

        return new ProfileCreated(
            profileName,
            profileExists,
            request.setDefault
        );
    }

    private Optional<Organization> validateTokenAndFetchOrg(OrgGateway orgGateway) {
        return AstraLogger.loading("Validating your Astra token", (_) -> {
            try {
                return Optional.of(orgGateway.current());
            } catch (AuthenticationException e) {
                return Optional.empty();
            }
        });
    }

    private ProfileName resolveProfileName(Organization org, CreateConfigRequest request) {
        return request.profileName.orElse(ProfileName.mkUnsafe(org.getName()));
    }

    private Optional<ConfigCreateResult> assertCanOverwriteProfile(ProfileName profileName, CreateConfigRequest request) {
        if (request.overwrite.isEmpty()) {
            request.assertCanOverwriteProfile.accept(profileName);
            return Optional.empty();
        }

        val shouldOverwrite = request.overwrite.get();

        return (!shouldOverwrite)
            ? Optional.of(new ProfileIllegallyExists(profileName))
            : Optional.empty();
    }
}
