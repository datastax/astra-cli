package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.CliConstants.$Env;
import com.dtsx.astra.cli.core.CliConstants.$Token;
import com.dtsx.astra.cli.core.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.exceptions.internal.misc.InvalidTokenException;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.SetupOperation;
import com.dtsx.astra.cli.operations.SetupOperation.InvalidToken;
import com.dtsx.astra.cli.operations.SetupOperation.ProfileCreated;
import com.dtsx.astra.cli.operations.SetupOperation.SetupRequest;
import com.dtsx.astra.cli.operations.SetupOperation.SetupResult;
import com.dtsx.astra.cli.utils.StringUtils;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.core.output.ExitCode.INVALID_TOKEN;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "setup",
    description = "Interactively set up the Astra CLI and create profiles"
)
public class SetupCmd extends AbstractCmd<SetupResult> {
    @Option(
        names = { $Token.LONG, $Token.SHORT },
        description = "Token to use authenticate each call. If not provided, you will be prompted for it",
        paramLabel = $Token.LABEL
    )
    public Optional<AstraToken> $token;

    @Option(
        names = { $Env.LONG, $Env.SHORT },
        description = "Astra environment for the token to target. If not provided, you will be prompted for it",
        completionCandidates = AstraEnvCompletion.class,
        paramLabel = $Env.LABEL
    )
    public Optional<AstraEnvironment> $env;

    @Option(
        names = { "--name" },
        description = "Optional name for the profile. If not provided, you will be prompted for it",
        paramLabel = "NAME"
    )
    public Optional<ProfileName> $name;

    @Override
    protected final OutputHuman executeHuman(Supplier<SetupResult> result) {
        return switch (result.get()) {
            case ProfileCreated pc -> handleProfileCreated(pc);
            case InvalidToken(var hint) -> throwInvalidToken(hint);
        };
    }

    private OutputHuman handleProfileCreated(ProfileCreated result) {
        val creationMessage = NL + ((result.overwritten())
            ? "@|bold Profile|@ %s @|bold successfully overwritten.|@".formatted(highlight(result.profileName()))
            : "@|bold Profile|@ %s @|bold successfully created.|@".formatted(highlight(result.profileName())));

        if (result.isDefault()) {
            return OutputHuman.response(creationMessage + "\n\nIt has been set as the default profile.", List.of(
                new Hint("Try it out!", "${cli.name} db list"),
                new Hint("Manage your profiles", "${cli.name} config list")
            ));
        }

        return OutputHuman.response(creationMessage, List.of(
            new Hint("Set it as the default profile with:", "${cli.name} config use " + result.profileName()),
            new Hint("Explicitly use this profile in commands like:", "${cli.name} db list --profile " + result.profileName()),
            new Hint("Manage your profiles", "${cli.name} config list")
        ));
    }

    private <T> T throwInvalidToken(Optional<AstraEnvironment> hint) {
        val hintStr = hint
            .map((env) -> " It is, however, valid for @!" + env.name().toLowerCase() + "!@.")
            .orElse("");

        throw new AstraCliException(INVALID_TOKEN, """
          @|bold,red Error: The token you provided is invalid.|@
        
          The token is not a valid Astra token for the given Astra environment.%s
        
          If you are targeting a different environment, ensure that the right environment is set with the @!--env!@ option.
        """.formatted(hintStr));
    }

    @Override
    protected Operation<SetupResult> mkOperation() {
        return new SetupOperation(
            OrgGateway::mkDefault,
            OrgGateway.Stateless.mkDefault(),
            new SetupRequest(
                $token,
                $env,
                $name,
                this::assertShouldSetup,
                this::assertShouldContinueIfAlreadySetup,
                this::assertShouldOverwriteExistingProfile,
                this::promptForToken,
                this::promptForGuessedEnvConfirmation,
                this::promptForEnv,
                this::promptForName,
                this::promptShouldSetDefault
            )
        );
    }

    private void assertShouldSetup(Path existing) {
        AstraLogger.banner();

        if (AstraConsole.getConsole() == null) {
            throw new AstraCliException("""
              @|bold,red Error: Cannot run setup in non-interactive mode.|@
            
              The setup process requires user interaction, but no console is available.
            
              Please use @!${cli.name} config create!@ to programmatically create profiles instead.
            """);
        }

        val prompt = """
          @|bold Welcome to the Astra CLI setup!|@
        
          @|faint A configuration file with your profile will be created at|@ @|faint,italic %s|@
        
          If you'd prefer to provide credentials on a per-command basis rather than storing them in a file, you can either:
          - Use the per-command @!--token!@ flag to pass your existing @!AstraCS!@ token directly.
          - Use the per-command @!--config-file!@ flag to specify an existing @!.astrarc!@ file.
        
          %s
          %s
        """.formatted(
            existing,
            renderComment("Example:"),
            renderCommand("${cli.name} db list --token <your_token>")
        );

        AstraConsole.println(trimIndent(prompt));
        AstraConsole.println();
        AstraConsole.getConsole().readPassword(AstraConsole.format("Press @!Enter!@ to continue, or use @!Ctrl+C!@ to cancel. "));
        AstraConsole.println();
    }

    private void assertShouldContinueIfAlreadySetup(Path existing) {
        AstraLogger.banner();

        val prompt = """
          @|bold Looks like you're already set up!|@
        
          @|faint Your config file already exists at|@ @|faint,italic %s|@
        
          Hint: You can use the @!${cli.name} config!@ commands to manage your profiles.
       
          %s
          %s
        
          Do you want to continue and create a new profile?
        """.formatted(
            existing,
            renderComment("Example:"),
            renderCommand("${cli.name} config list")
        );

        val shouldContinue = AstraConsole.confirm(prompt)
            .defaultYes()
            .fallbackFlag("")
            .fix(List.of(), "")
            .clearAfterSelection();

        if (!shouldContinue) {
            throw new ExecutionCancelledException();
        }
    }

    private void assertShouldOverwriteExistingProfile(Profile existing) {
        val prompt = """
          @|bold A profile with this name already exists with token|@ %s @|bold for environment|@ %s@|bold .|@
        
          You can use @!${cli.name} config get %s!@ to get more information about the existing profile.
        
          Do you wish to overwrite it?
        """.formatted(
            highlight(existing.token().toString()),
            highlight(existing.env().name().toLowerCase()),
            existing.name().orElseThrow()
        );

        val shouldOverwrite = AstraConsole.confirm(prompt)
            .defaultNo()
            .fallbackFlag("")
            .fix(List.of(), "")
            .clearAfterSelection();

        if (!shouldOverwrite) {
            throw new ExecutionCancelledException();
        }
    }

    private AstraToken promptForToken() {
        val prompt = AstraColors.PURPLE_300.use("(Required)") + " Enter your Astra token (it should start with @!AstraCS!@)";

        return AstraConsole.prompt(prompt)
            .mapper(input -> AstraToken.parse(input).getRight(InvalidTokenException::new))
            .echoOff(StringUtils::maskToken)
            .requireAnswer()
            .fallbackFlag("--token")
            .fix(originalArgs(), "--token <your_token>")
            .dontClearAfterSelection();
    }

    private boolean promptForGuessedEnvConfirmation(AstraEnvironment env) {
        val prompt = """
          It looks like your token is valid for the @!%s!@ environment. Do you want to use this environment?
        """.formatted(env.name().toLowerCase());

        return AstraConsole.confirm(prompt)
            .defaultYes()
            .fallbackFlag("")
            .fix(List.of(), "")
            .clearAfterSelection();
    }

    private AstraEnvironment promptForEnv(AstraEnvironment defaultEnv) {
        val prompt = AstraColors.PURPLE_300.use("(Optional)") + " Enter the target Astra environment (defaults to @!prod!@)";

        return AstraConsole.select(prompt)
            .options(AstraEnvironment.values())
            .defaultOption(defaultEnv)
            .mapper(e -> e.name().toLowerCase())
            .fallbackFlag("--env")
            .fix(originalArgs(), "--env <prod|test|dev>")
            .dontClearAfterSelection();
    }

    private ProfileName promptForName(String defaultName) {
        val prompt = AstraColors.PURPLE_300.use("(Optional)") + " Enter a name for your profile (defaults to your org name)";

        return AstraConsole.prompt(prompt)
            .mapper(ProfileName::mkUnsafe)
            .defaultOption(defaultName)
            .fallbackFlag("--name")
            .fix(originalArgs(), "--name <profile_name>")
            .dontClearAfterSelection();
    }

    private Boolean promptShouldSetDefault() {
        val prompt = """
          @|bold A default profile already exists.|@
        
          Do you want to set this profile as the default instead?
        """;

        return AstraConsole.confirm(prompt)
            .defaultNo()
            .fallbackFlag("")
            .fix(List.of(), "")
            .clearAfterSelection();
    }
}
