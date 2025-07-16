package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.operations.SetupOperation.SetupResult;
import com.dtsx.astra.sdk.exception.AuthenticationException;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.File;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class SetupOperation implements Operation<SetupResult> {
    private final BiFunction<AstraToken, AstraEnvironment, OrgGateway> createOrgGateway;
    private final SetupRequest request;

    public record SetupRequest(
        Optional<AstraToken> token,
        Optional<AstraEnvironment> env,
        Optional<ProfileName> name,
        Consumer<File> assertShouldSetup,
        Consumer<File> assertShouldContinueIfAlreadySetup,
        Consumer<Profile> assertShouldOverwriteExistingProfile,
        Supplier<AstraToken> promptForToken,
        Supplier<Optional<AstraEnvironment>> promptForEnv,
        Supplier<Optional<ProfileName>> promptForName,
        Supplier<Boolean> promptShouldSetDefault
    ) {}

    public sealed interface SetupResult {}
    public record ProfileCreated(ProfileName profileName, boolean overwritten, boolean isDefault) implements SetupResult {}
    public record InvalidToken() implements SetupResult {}

    @Override
    public SetupResult execute() {
        val configFile = AstraConfig.resolveDefaultAstraConfigFile();
        val config = AstraConfig.readAstraConfigFile(null);

        if (configFile.exists() && !config.getProfiles().isEmpty()) {
            request.assertShouldContinueIfAlreadySetup.accept(configFile);
        } else {
            request.assertShouldSetup.accept(configFile);
        }

        return resolveProfileDetails(request).foldMap((details) -> {
            config.lookupProfile(details.profileName).ifPresent(request.assertShouldOverwriteExistingProfile);

            val shouldSetDefault = (config.lookupProfile(ProfileName.DEFAULT).isPresent())
                ? request.promptShouldSetDefault.get()
                : true;

            config.modify((ctx) -> {
                ctx.deleteProfile(details.profileName);
                ctx.createProfile(details.profileName, details.token, details.env);

                if (shouldSetDefault) {
                    ctx.deleteProfile(ProfileName.DEFAULT);
                    ctx.createProfile(ProfileName.DEFAULT, details.token, details.env);
                }
            });

            return new ProfileCreated(details.profileName, false, shouldSetDefault);
        });
    }

    private record ProfileDetails(
        ProfileName profileName,
        AstraToken token,
        AstraEnvironment env
    ) {}

    private Either<SetupResult, ProfileDetails> resolveProfileDetails(SetupRequest request) {
        val token = request.token.orElseGet(request.promptForToken);
        val env = request.env.or(request.promptForEnv).orElse(AstraEnvironment.PROD);

        val orgGateway = createOrgGateway.apply(token, env);
        val org = validateTokenAndFetchOrg(orgGateway);

        if (org.isEmpty()) {
            return Either.left(new InvalidToken());
        }

        val profileName = resolveProfileName(org.get(), request);

        return Either.right(
            new ProfileDetails(profileName, token, env)
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

    private ProfileName resolveProfileName(Organization org, SetupRequest request) {
        return request.name.or(request.promptForName).orElse(ProfileName.mkUnsafe(org.getName()));
    }
}
