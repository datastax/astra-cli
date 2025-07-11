package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigCreateOperation;
import com.dtsx.astra.cli.operations.config.ConfigCreateOperation.*;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.*;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.utils.StringUtils.NL;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

@Command(
    name = "create",
    description = "Create a new configuration profile, to store your Astra credentials. Use the `--token @<file>` syntax to securely read the token from a file, without leaking the token to your shell history."
)
@Example(
    comment = "Create a new profile with an explicit name",
    command = "astra config create my_profile -t AstraCS:..."
)
@Example(
    comment = { "(Recommended)", "Securely create a new profile with the token provided from a plaintext file" },
    command = "astra config create my_profile -t @token.txt"
)
@Example(
    comment = "Create a new profile with the default name (organization name)",
    command = "astra config create -t AstraCS:..."
)
@Example(
    comment = "Create a new profile and set it as the default profile",
    command = "astra config create my_profile -t AstraCS:... --default"
)
@Example(
    comment = "Create a new profile without any prompting if the profile already exists",
    command = "astra config create my_profile -t AstraCS:... -y"
)
public class ConfigCreateCmd extends AbstractConfigCmd<ConfigCreateResult> {
    @Parameters(
        arity = "0..1",
        description = "Profile name (defaults to organization name if not specified)",
        paramLabel = "PROFILE"
    )
    public Optional<ProfileName> $profileName;

    @Option(
        names = { "-t", "--token" },
        description = "Astra authentication token (must start with 'AstraCS:')",
        paramLabel = "TOKEN",
        required = true
    )
    public AstraToken $token;

    @Option(
        names = { "-e", "--env" },
        description = { "Astra environment to connect to", DEFAULT_VALUE },
        completionCandidates = AstraEnvCompletion.class,
        paramLabel = "ASTRA_ENV",
        defaultValue = "prod"
    )
    public AstraEnvironment $env;

    @Option(
        names = { "-d", "--default" },
        description = { "Set the created profile as the default profile", DEFAULT_VALUE }
    )
    public boolean $setDefault;

    @ArgGroup
    public @Nullable ExistingProfileBehavior $existingProfileBehavior;

    public static class ExistingProfileBehavior {
        @Option(names = { "-y", "--yes" }, description = "Force creation without any confirmation prompts")
        public boolean force;

        @Option(names = { "-F", "--fail-if-exists" }, description = "Fail if profile already exists instead of prompting")
        public boolean failIfExists;
    }

    @Override
    public final OutputAll execute(ConfigCreateResult result) {
        return switch (result) {
            case ProfileCreated pc -> handleConfigCreated(pc);
            case ProfileIllegallyExists(var profileName) -> throwProfileAlreadyExists(profileName);
            case ViolatedFailIfExists() -> throwAttemptedToSetDefault();
            case InvalidToken() -> throwInvalidToken();
        };
    }

    private OutputAll handleConfigCreated(ProfileCreated result) {
        val creationMessage = (result.overwritten())
            ? "Configuration profile %s successfully overwritten.".formatted(highlight(result.profileName()))
            : "Configuration profile %s successfully created.".formatted(highlight(result.profileName()));

        val data = mkData(result.profileName(), result.isDefault(), result.overwritten());

        if (result.isDefault()) {
            return OutputAll.response(creationMessage + NL + NL + "It is now the default profile.", data);
        }

        return OutputAll.response(creationMessage, data, List.of(
            new Hint("Set it as the default profile:", "astra config use " + result.profileName())
        ));
    }

    private <T> T throwProfileAlreadyExists(ProfileName profileName) {
        val wasFailIfExists = originalArgs().contains("--fail-if-exists") || originalArgs().contains("-F");

        val originalArgsWithoutFailIfExists = originalArgs().stream()
            .filter(arg -> !arg.equals("--fail-if-exists") && !arg.equals("-F"))
            .toList();

        val mainMsg = (wasFailIfExists)
            ? "The @!--fail-if-exists!@ flag was set, so the operation failed without confirmation or warning."
            : "To overwrite it, either interactively respond to the prompt, or use the @!--yes!@ option to proceed without confirmation.";

        throw new AstraCliException(CONFIG_ALREADY_EXISTS, """
          @|bold,red Error: A profile with the name '%s' already exists in the configuration file.|@

          %s
        """.formatted(
            profileName,
            mainMsg
        ), List.of(
            new Hint("Example fix:", originalArgsWithoutFailIfExists, "--yes"),
            new Hint("See the values of the existing profile:", "astra config get " + profileName)
        ));
    }

    private <T> T throwAttemptedToSetDefault() {
        val defaultFlag = (!originalArgs().contains("--default") && !originalArgs().contains("-d"))
            ? "--default"
            : "";

        val originalArgsWithoutDefault = originalArgs().stream()
            .map((s) -> ProfileName.DEFAULT.unwrap().equals(s) ? "<new_name>" : s)
            .toList();

        throw new AstraCliException(ILLEGAL_OPERATION, """
          @|bold,red Error: Cannot set the default profile directly.|@

          Please create the profile using a different name, then:
          - Pass the @!--default!@ flag while creating the profile, or
          - Create the profile, then run @!astra config use <profile>!@ to set it as default.
        """, List.of(
            new Hint("Example fix (replacing <new_name>):", originalArgsWithoutDefault, defaultFlag)
        ));
    }

    private <T> T throwInvalidToken() {
        throw new AstraCliException(INVALID_TOKEN, """
          @|bold,red Error: The token you provided is invalid.|@
        
          The token %s matches the expect format, but is not a valid Astra token.
        
          If you are targeting a non-production environment, ensure that the right environment is set with the @!--env!@ option.
        """.formatted(
            highlight($token)
        ));
    }

    @Override
    public Operation<ConfigCreateResult> mkOperation() {
        return new ConfigCreateOperation(config(), OrgGateway.mkDefault($token, $env), new CreateConfigRequest(
            $profileName,
            $token,
            $env,
            Optional.ofNullable($existingProfileBehavior).map(eb -> eb.force).orElse(false),
            Optional.ofNullable($existingProfileBehavior).map(eb -> eb.failIfExists).orElse(false),
            $setDefault,
            this::assertCanOverwriteProfile
        ));
    }

    private void assertCanOverwriteProfile(ProfileName profileName) {
        val confirmationMsg = """
          A profile under the name %s already exists in the given configuration file.

          Do you wish to overwrite it? [y/N]
        """.formatted(
            highlight(profileName)
        );

        switch (AstraConsole.confirm(trimIndent(confirmationMsg))) {
            case ANSWER_NO -> throw new ExecutionCancelledException("Operation cancelled by user. Use --yes to override without confirmation.");
            case NO_ANSWER -> throwProfileAlreadyExists(profileName);
        }
    }

    private Map<String, Object> mkData(ProfileName profileName, Boolean isDefault, Boolean wasOverwritten) {
        return Map.of(
            "profileName", profileName,
            "isDefault", isDefault,
            "wasOverwritten", wasOverwritten
        );
    }
}
