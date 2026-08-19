package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigCreateOperation.ConfigCreateResult;
import com.dtsx.astra.sdk.exception.AuthenticationException;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class ConfigCreateOperation implements Operation<ConfigCreateResult> {
    private final CliContext ctx;
    private final AstraConfig config;
    private final Function<AstraToken, OrgGateway> orgGateway;
    private final OrgGateway.Stateless statelessOrgGateway;
    private final CreateConfigRequest request;

    public record CreateConfigRequest(
        Optional<ProfileName> profileName,
        Optional<AstraToken> token,
        AstraEnvironment env,
        Optional<Boolean> overwrite,
        boolean setDefault,
        boolean validate,
        Optional<String> localEndpoint,
        Consumer<ProfileName> assertCanOverwriteProfile
    ) {}

    public sealed interface ConfigCreateResult {}
    public record ProfileCreated(ProfileName profileName, boolean overwritten, boolean isDefault) implements ConfigCreateResult {}
    public record ProfileIllegallyExists(ProfileName profileName) implements ConfigCreateResult {}
    public record ViolatedFailIfExists() implements ConfigCreateResult {}
    public record MissingToken() implements ConfigCreateResult {}
    public record InvalidToken(Optional<AstraEnvironment> hint) implements ConfigCreateResult {}
    public record NameRequiredIfNotValidated() implements ConfigCreateResult {}

    @Override
    public ConfigCreateResult execute() {
        val maybeProfileName = validateTokenAndResolveName(request);
        if (maybeProfileName.isLeft()) {
            return maybeProfileName.getLeft();
        }

        val profileName = maybeProfileName.getRight();
        val profileExists = config.profileExists(profileName);

        if (profileExists) {
            val res = assertCanOverwriteProfile(profileName, request);

            if (res.isPresent()) {
                return res.get();
            }
        }

        val maybeToken = resolveToken(request.token, request.env);
        if (maybeToken.isLeft()) {
            return maybeToken.getLeft();
        }

        config.modify((ctx) -> {
            ctx.deleteProfile(profileName);
            ctx.createProfile(profileName, maybeToken.getRight(), request.env, request.localEndpoint);

            if (request.setDefault) {
                ctx.copyProfile(config.lookupProfile(profileName).orElseThrow(), ProfileName.DEFAULT);
            }
        });

        return new ProfileCreated(
            profileName,
            profileExists,
            request.setDefault
        );
    }

    private Either<ConfigCreateResult, ProfileName> validateTokenAndResolveName(CreateConfigRequest request) {
        if (!request.validate || request.token.isEmpty()) {
            return Either.fromOptional(request.profileName, NameRequiredIfNotValidated::new);
        }

        val token = request.token.get();

        return ctx.log().loading("Validating your Astra token", (_) -> {
            try {
                val orgName = ProfileName.mkUnsafe(orgGateway.apply(token).current().getName());
                return Either.pure(request.profileName.orElse(orgName));
            } catch (AuthenticationException e) {
                val validEnv = statelessOrgGateway.resolveOrganizationEnvironment(token).map(Pair::getLeft);
                return Either.left(new InvalidToken(validEnv));
            } catch (Exception e) {
                ctx.log().hint("You can use @!--no-validate!@ to skip token validation and create a profile with an explicit name.");
                throw e;
            }
        });
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

    private Either<ConfigCreateResult, AstraToken> resolveToken(Optional<AstraToken> token, AstraEnvironment env) {
        if (token.isPresent()) {
            return Either.pure(token.get());
        }

        if (env.isLocal()) {
            ctx.log().info("@|italic Using a fake astra token for your local environment.|@");
            ctx.log().info("@|italic Provide an explicit token if using an actual auth manager.|@");
            return Either.pure(AstraToken.FAKE);
        }

        return Either.left(new MissingToken());
    }
}
