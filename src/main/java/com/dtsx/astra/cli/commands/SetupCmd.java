package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.exceptions.internal.misc.InvalidTokenException;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraConsole.ConfirmResponse;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.SetupOperation;
import com.dtsx.astra.cli.operations.SetupOperation.InvalidToken;
import com.dtsx.astra.cli.operations.SetupOperation.ProfileCreated;
import com.dtsx.astra.cli.operations.SetupOperation.SetupRequest;
import com.dtsx.astra.cli.operations.SetupOperation.SetupResult;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.INVALID_TOKEN;
import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.NO_ANSWER;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "setup",
    description = "Initialize the Astra CLI configuration file"
)
public class SetupCmd extends AbstractCmd<SetupResult> {
    @Option(
        names = { "--token" },
        description = "Token to use authenticate each call. If not provided, you will be prompted for it",
        paramLabel = "TOKEN"
    )
    public Optional<AstraToken> $token;

    @Option(
        names = { "--env" },
        description = "Astra environment for the token to target. If not provided, you will be prompted for it",
        completionCandidates = AstraEnvCompletion.class,
        paramLabel = "ENV"
    )
    public Optional<AstraEnvironment> $env;

    @Option(
        names = { "--name" },
        description = "Optional name for the profile. If not provided, you will be prompted for it",
        paramLabel = "NAME"
    )
    public Optional<ProfileName> $name;

    @Override
    protected OutputHuman executeHuman(SetupResult result) {
        return switch (result) {
            case ProfileCreated pc -> handleProfileCreated(pc);
            case InvalidToken() -> throwInvalidToken();
        };
    }

    private OutputHuman handleProfileCreated(ProfileCreated result) {
        val creationMessage = NL + ((result.overwritten())
            ? "@|bold Profile|@ %s @|bold successfully overwritten.|@".formatted(highlight(result.profileName()))
            : "@|bold Profile|@ %s @|bold successfully created.|@".formatted(highlight(result.profileName())));

        if (result.isDefault()) {
            return OutputHuman.response(creationMessage + "\n\nIt is now the default profile.", null);
        }

        return OutputHuman.response(creationMessage, List.of(
            new Hint("Set it as the default profile with:", "astra config use " + result.profileName())
        ));
    }

    private <T> T throwInvalidToken() {
        throw new AstraCliException(INVALID_TOKEN, """
          @|bold,red Error: The token you provided is invalid.|@
        
          The token is not a valid Astra token.
        
          If you are targeting a non-production environment, ensure that the right environment is set with the @!--env!@ option.
        """);
    }

    @Override
    protected Operation<SetupResult> mkOperation() {
        return new SetupOperation(
            OrgGateway::mkDefault,
            new SetupRequest(
                $token,
                $env,
                $name,
                this::assertShouldSetup,
                this::assertShouldContinueIfAlreadySetup,
                this::assertShouldOverwriteExistingProfile,
                this::promptForToken,
                this::promptForEnv,
                this::promptForName,
                this::promptShouldSetDefault
            )
        );
    }

    private void assertShouldSetup(File existing) {
        AstraLogger.banner();

        val confirmationMsg = """
          Welcome to the Astra CLI setup! A configuration file with your profile will be created at %s.
        
          If you'd prefer to provide credentials on a per-command basis rather than storing them in a file, you can either:
          - Use the @!--token!@ option to pass your token directly.
          - Use the @!--config-file!@ option to specify a different configuration file.
        
          %s
          %s
        
          Enter anything to continue, or press %s to cancel.
        """.formatted(
            highlight(existing.getAbsolutePath()),
            renderComment("Example:"),
            renderCommand("astra db list --token <your_token>"),
            AstraColors.PURPLE_300.use("Ctrl+C")
        );

        if (AstraConsole.confirm(trimIndent(confirmationMsg)) == ConfirmResponse.ANSWER_NO) {
            throw new ExecutionCancelledException("Operation cancelled by user.");
        }
    }

    private void assertShouldContinueIfAlreadySetup(File existing) {
        AstraLogger.banner();

        val confirmationMsg = """
          Looks like you're already set up—your config file exists at %s.
        
          Hint: You can use the @!astra config!@ commands to manage your profiles.
       
          %s
          %s
        
          Do you want to continue and create a new profile? [Y/n]
        """.formatted(
            highlight(existing.getAbsolutePath()),
            renderComment("Example:"),
            renderCommand("astra config list")
        );

        if (AstraConsole.confirm(trimIndent(confirmationMsg)) == ConfirmResponse.ANSWER_NO) {
            throw new ExecutionCancelledException("Operation cancelled by user.");
        }
    }

    private void assertShouldOverwriteExistingProfile(Profile existing) {
        val confirmationMsg = """
          @|bold A profile with this name already exists with token|@ %s @|bold for environment|@ %s@|bold .|@
        
          You can use @!astra config get %s!@ to get more information about the existing profile.
        
          Do you wish to overwrite it? [y/N]
        """.formatted(
            highlight(existing.token().toString()),
            highlight(existing.env().name().toLowerCase()),
            existing.name().orElseThrow()
        );

        switch (AstraConsole.confirm(NL + trimIndent(confirmationMsg))) {
            case ANSWER_NO -> throw new ExecutionCancelledException("Operation cancelled by user.");
            case NO_ANSWER -> throw new AstraCliException(NO_ANSWER, "Please answer interactively or pass the --name option with a different profile name.");
        }
    }

    private AstraToken promptForToken() {
        val message = """
        
        %s Enter your Astra token, or the path to the file containing it
        @!>!@"""
            .stripIndent()
            .formatted(AstraColors.PURPLE_300.use("(Required)"));

        val tokenInput = AstraConsole.readLine(message, true);

        return tokenInput.map(AstraToken::parse)
            .map(r -> r.getRight(InvalidTokenException::new))
            .orElseThrow(() -> new AstraCliException(NO_ANSWER, "An Astra token is required. Please provide it interactively or pass the --token option."));
    }

    private Optional<AstraEnvironment> promptForEnv() {
        val message = """
        
        %s Enter the target Astra environment (defaults to @!prod!@)
        @!>!@"""
            .stripIndent()
            .formatted(AstraColors.PURPLE_300.use("(Optional)"));

        val envInput = AstraConsole.readLine(message, false);

        return envInput.map((input) -> {
            if (input.isBlank()) {
                return AstraEnvironment.PROD;
            }
            return AstraEnvironment.valueOf(input.trim().toUpperCase());
        });
    }

    private Optional<ProfileName> promptForName() {
        val message = """
        
        %s Enter a name for your profile (defaults to your org's name)
        @!>!@"""
            .stripIndent()
            .formatted(AstraColors.PURPLE_300.use("(Optional)"));

        val nameInput = AstraConsole.readLine(message, false);

        return nameInput.filter(name -> !name.trim().isEmpty())
            .map(ProfileName::mkUnsafe);
    }

    private Boolean promptShouldSetDefault() {
        val confirmationMsg = """
          @|bold A default profile already exists.|@
        
          Do you want to set this profile as the default instead? [y/N]
        """;

        return switch (AstraConsole.confirm(NL + trimIndent(confirmationMsg))) {
            case ANSWER_OK -> true;
            case ANSWER_NO, NO_ANSWER -> false;
        };
    }
}
