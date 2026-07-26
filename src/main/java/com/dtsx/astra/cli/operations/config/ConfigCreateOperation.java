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

@RequiredArgsConstructor
public class ConfigCreateOperation implements Operation<ConfigCreateResult> {
    private final CliContext ctx;
    private final AstraConfig config;
    private final OrgGateway orgGateway;
    private final OrgGateway.Stateless statelessOrgGateway;
    private final CreateConfigRequest request;

    public record CreateConfigRequest(
        Optional<ProfileName> profileName,
        AstraToken token,
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
    public record InvalidToken(Optional<AstraEnvironment> hint) implements ConfigCreateResult {}
    public record NameRequiredIfNotValidated() implements ConfigCreateResult {}

    @Override
    public ConfigCreateResult execute() {
        val maybeProfileName = validateTokenAndResolveName(orgGateway, request);

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

        config.modify((ctx) -> {
            ctx.deleteProfile(profileName);
            ctx.createProfile(profileName, request.token, request.env, request.localEndpoint);

            if (request.setDefault) {
                ctx.deleteProfile(ProfileName.DEFAULT);
                ctx.createProfile(ProfileName.DEFAULT, request.token, request.env, request.localEndpoint);
            }
        });

        return new ProfileCreated(
            profileName,
            profileExists,
            request.setDefault
        );
    }

    private Either<ConfigCreateResult, ProfileName> validateTokenAndResolveName(OrgGateway orgGateway, CreateConfigRequest request) {
        if (!request.validate) {
            return Either.fromOptional(request.profileName, NameRequiredIfNotValidated::new);
        }

        return ctx.log().loading("Validating your Astra token", (_) -> {
            try {
                val name = ProfileName.mkUnsafe(orgGateway.current().getName());
                return Either.pure(name);
            } catch (AuthenticationException e) {
                val validEnv = statelessOrgGateway.resolveOrganizationEnvironment(request.token).map(Pair::getLeft);
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
}
