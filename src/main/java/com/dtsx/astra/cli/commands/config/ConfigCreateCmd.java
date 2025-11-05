package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.core.CliConstants.$Env;
import com.dtsx.astra.cli.core.CliConstants.$Profile;
import com.dtsx.astra.cli.core.CliConstants.$Token;
import com.dtsx.astra.cli.core.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigCreateOperation;
import com.dtsx.astra.cli.operations.config.ConfigCreateOperation.*;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.*;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;
import static com.dtsx.astra.cli.utils.StringUtils.NL;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

@Command(
    name = "create",
    description = "Create a new configuration profile, to store your Astra credentials. Use the `--token @<file>` syntax to securely read the token from a file, without leaking the token to your shell history."
)
@Example(
    comment = "(Recommended) Interactively create a new profile",
    command = "${cli.name} setup"
)
@Example(
    comment = "Programmatically create a new profile",
    command = "${cli.name} config create my_profile -t AstraCS:..."
)
@Example(
    comment = "(Recommended) Securely create a new profile with the token provided from a file",
    command = "${cli.name} config create my_profile -t @token.txt"
)
@Example(
    comment = "Create a new profile with the default name (organization name)",
    command = "${cli.name} config create -t AstraCS:..."
)
@Example(
    comment = "Create a new profile and set it as the default profile",
    command = "${cli.name} config create my_profile -t AstraCS:... --default"
)
public class ConfigCreateCmd extends AbstractConfigCmd<ConfigCreateResult> {
    @Parameters(
        arity = "0..1",
        description = { "Profile name", SHOW_CUSTOM_DEFAULT + "organization name" },
        paramLabel = $Profile.LABEL
    )
    public Optional<ProfileName> $profileName;

    @Option(
        names = { $Token.LONG, $Token.SHORT },
        description = "Astra authentication token",
        paramLabel = $Token.LABEL,
        required = true
    )
    public AstraToken $token;

    @Option(
        names = { $Env.LONG, $Env.SHORT },
        description = "Astra environment to connect to",
        completionCandidates = AstraEnvCompletion.class,
        defaultValue = $Env.DEFAULT,
        paramLabel = $Env.LABEL
    )
    public AstraEnvironment $env;

    @Option(
        names = { "-d", "--default" },
        description = "Set the created profile as the default profile"
    )
    public boolean $setDefault;

    @Option(
        names = { "--overwrite" },
        description = "Overwrite existing profile(s) with the same name",
        paramLabel = "PRINT",
        negatable = true
    )
    private Optional<Boolean> $overwrite;

    @Override
    public final OutputAll execute(Supplier<ConfigCreateResult> resultSupplier) {
        val result = resultSupplier.get();

        return switch (result) {
            case ProfileCreated pc -> handleConfigCreated(pc);
            case ProfileIllegallyExists(var profileName) -> throwProfileAlreadyExists(profileName);
            case ViolatedFailIfExists() -> throwAttemptedToSetDefault();
            case InvalidToken(var hint) -> throwInvalidToken(hint);
        };
    }

    private OutputAll handleConfigCreated(ProfileCreated result) {
        val creationMessage = (result.overwritten())
            ? "Configuration profile %s successfully overwritten.".formatted(ctx.highlight(result.profileName()))
            : "Configuration profile %s successfully created.".formatted(ctx.highlight(result.profileName()));

        val data = mkData(result.profileName(), result.isDefault(), result.overwritten());

        if (result.isDefault()) {
            return OutputAll.response(creationMessage + mkHint() + NL + NL + "It is now the default profile.", data);
        }

        return OutputAll.response(creationMessage + mkHint(), data, List.of(
            new Hint("Set it as the default profile:", "${cli.name} config use " + result.profileName())
        ));
    }

    private <T> T throwProfileAlreadyExists(ProfileName profileName) {
        val wasFailIfExists = originalArgs().contains("--fail-if-exists") || originalArgs().contains("-F");

        val originalArgsWithoutFailIfExists = originalArgs().stream()
            .filter(arg -> !arg.equals("--fail-if-exists") && !arg.equals("-F"))
            .toList();

        val mainMsg = (wasFailIfExists)
            ? "The @'!--fail-if-exists!@ flag was set, so the operation failed without confirmation or warning."
            : "To overwrite it, either interactively respond @'!yes@! to the prompt, or use the @'!--overwrite!@ option to proceed without confirmation.";

        throw new AstraCliException(CONFIG_ALREADY_EXISTS, """
          @|bold,red Error: A profile with the name '%s' already exists in the configuration file.|@

          %s
        """.formatted(
            profileName,
            mainMsg
        ), List.of(
            new Hint("Example fix:", originalArgsWithoutFailIfExists, "--overwrite"),
            new Hint("See the values of the existing profile:", "${cli.name} config get " + profileName)
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
          - Pass the @'!--default!@ flag while creating the profile, or
          - Create the profile, then run @'!${cli.name} config use <profile>!@ to set it as default.
        """, List.of(
            new Hint("Example fix (replacing <new_name>):", originalArgsWithoutDefault, defaultFlag)
        ));
    }

    private <T> T throwInvalidToken(Optional<AstraEnvironment> hint) {
        val hintStr = hint
            .map((env) -> " It is, however, valid for @!" + env.name().toLowerCase() + "!@.")
            .orElse("");

        throw new AstraCliException(INVALID_TOKEN, """
          @|bold,red Error: The token you provided is invalid.|@
        
          The token is not a valid Astra token for the given Astra environment.%s
        
          If you are targeting a different environment, ensure that the right environment is set with the @'!--env!@ option.
        """.formatted(hintStr));
    }

    @Override
    public Operation<ConfigCreateResult> mkOperation() {
        return new ConfigCreateOperation(ctx, config(true), ctx.gateways().mkOrgGateway($token, $env), ctx.gateways().mkOrgGatewayStateless(), new CreateConfigRequest(
            $profileName,
            $token,
            $env,
            $overwrite,
            $setDefault,
            this::assertCanOverwriteProfile
        ));
    }

    private void assertCanOverwriteProfile(ProfileName profileName) {
        val msg = trimIndent("""
          A profile under the name @'!%s!@ already exists in the given configuration file.

          Do you wish to overwrite it?
        """).formatted(profileName);

        val allowOverwrite = ctx.console().confirm(msg)
            .defaultNo()
            .fallbackFlag("--overwrite")
            .fix(originalArgs(), "--overwrite")
            .clearAfterSelection();

        if (!allowOverwrite) {
            throwProfileAlreadyExists(profileName);
        }
    }

    private String mkHint() {
        return (ctx.outputIsHuman() && ctx.isTty())
            ? NL + NL + "(Hint: Use @'!${cli.name} setup!@ for an interactive profile creation experience!)"
            : "";
    }

    private LinkedHashMap<String, Object> mkData(ProfileName profileName, Boolean isDefault, Boolean wasOverwritten) {
        return sequencedMapOf(
            "profileName", profileName,
            "isDefault", isDefault,
            "wasOverwritten", wasOverwritten
        );
    }
}
