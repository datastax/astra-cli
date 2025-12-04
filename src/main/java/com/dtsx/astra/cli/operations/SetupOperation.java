package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.operations.SetupOperation.SetupResult;
import com.dtsx.astra.sdk.exception.AuthenticationException;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class SetupOperation implements Operation<SetupResult> {
    private final CliContext ctx;
    private final BiFunction<AstraToken, AstraEnvironment, OrgGateway> createOrgGateway;
    private final OrgGateway.Stateless statelessOrgGateway;
    private final SetupRequest request;

    public record SetupRequest(
        Optional<AstraToken> token,
        Optional<AstraEnvironment> env,
        Optional<ProfileName> name,
        Consumer<Path> assertShouldSetup,
        Consumer<Path> assertShouldContinueIfAlreadySetup,
        Consumer<Profile> assertShouldOverwriteExistingProfile,
        Supplier<AstraToken> promptForToken,
        Function<AstraEnvironment, AstraEnvironment> promptForEnv,
        BiFunction<String, AstraEnvironment, ProfileName> promptForName,
        Supplier<Boolean> promptShouldSetDefault
    ) {}

    public sealed interface SetupResult {}
    public record SameProfileAlreadyExists(ProfileName profileName) implements SetupResult {}
    public record ProfileCreated(ProfileName profileName, boolean overwritten, boolean isDefault) implements SetupResult {}
    public record InvalidToken(Optional<AstraEnvironment> hint) implements SetupResult {}

    @Override
    public SetupResult execute() {
        val configFile = AstraConfig.resolveDefaultAstraConfigFile(ctx);

        if (ifConfigExistsAnd(c -> !c.profiles().isEmpty())) {
            request.assertShouldContinueIfAlreadySetup.accept(configFile);
        } else {
            request.assertShouldSetup.accept(configFile);
        }

        return resolveProfileDetails(request).map((details) -> {
            if (Files.exists(configFile)) {
                val existingProfile = config().lookupProfile(details.profileName);

                if (existingProfile.isPresent()) {
                    val existingProfileIsSame =
                        existingProfile.get().name().equals(Optional.ofNullable(details.profileName)) &&
                        existingProfile.get().token().equals(details.token) &&
                        existingProfile.get().env().equals(details.env);

                    if (existingProfileIsSame) {
                        return new SameProfileAlreadyExists(details.profileName);
                    } else {
                        request.assertShouldOverwriteExistingProfile.accept(existingProfile.get());
                    }
                }
            }

            val shouldSetDefault = (ifConfigExistsAnd(c -> c.profileExists(ProfileName.DEFAULT)))
                ? request.promptShouldSetDefault.get()
                : true;

            config().modify((ctx) -> {
                ctx.deleteProfile(details.profileName);
                ctx.createProfile(details.profileName, details.token, details.env);

                if (shouldSetDefault) {
                    ctx.deleteProfile(ProfileName.DEFAULT);
                    ctx.createProfile(ProfileName.DEFAULT, details.token, details.env); // TODO needs source!!!
                }
            });

            return new ProfileCreated(details.profileName, false, shouldSetDefault);
        }).fold(l -> l, r -> r);
    }

    private record ProfileDetails(
        ProfileName profileName,
        AstraToken token,
        AstraEnvironment env
    ) {}

    private Either<SetupResult, ProfileDetails> resolveProfileDetails(SetupOperation.SetupRequest request) {
        val token = request.token.orElseGet(request.promptForToken);

        val envAndOrg = resolveEnvAndOrg(token);

        if (envAndOrg.isLeft()) {
            return Either.left(new InvalidToken(envAndOrg.getLeft()));
        }

        val env = envAndOrg.getRight().getLeft();

        val profileName = resolveProfileName(envAndOrg.getRight().getRight(), env);

        return Either.pure(
            new ProfileDetails(profileName, token, env)
        );
    }

    private Either<Optional<AstraEnvironment>, Pair<AstraEnvironment, Organization>> resolveEnvAndOrg(AstraToken token) {
        var guessedEnv = Optional.<AstraEnvironment>empty();

        if (request.env().isEmpty()) {
            val guessed = statelessOrgGateway.resolveOrganizationEnvironment(token);

            if (guessed.isPresent()) {
                return Either.pure(guessed.get());
            }
        }

        val env = request.env().orElseGet(() -> request.promptForEnv.apply(AstraEnvironment.PROD));

        val orgGateway = createOrgGateway.apply(token, env);
        val org = validateTokenAndFetchOrg(orgGateway);

        return (org.isPresent())
            ? Either.pure(Pair.of(env, org.get()))
            : Either.left(guessedEnv);
    }

    private Optional<Organization> validateTokenAndFetchOrg(OrgGateway orgGateway) {
        return ctx.log().loading("Validating your Astra token", (_) -> {
            try {
                return Optional.of(orgGateway.current());
            } catch (AuthenticationException e) {
                return Optional.empty();
            }
        });
    }

    private ProfileName resolveProfileName(Organization org, AstraEnvironment env) {
        return request.name.orElseGet(() -> request.promptForName.apply(org.getName(), env));
    }

    private @Nullable AstraConfig cachedConfig;

    private AstraConfig config() {
        if (cachedConfig == null) {
            cachedConfig = AstraConfig.readAstraConfigFile(ctx, null, true);
        }
        return cachedConfig;
    }

    private boolean ifConfigExistsAnd(Function<AstraConfig, Boolean> fn) {
        val configFile = AstraConfig.resolveDefaultAstraConfigFile(ctx);

        if (Files.exists(configFile)) {
            return fn.apply(config());
        }

        return false;
    }
}
