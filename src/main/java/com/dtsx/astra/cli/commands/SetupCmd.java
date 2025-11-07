package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.CliConstants.$Env;
import com.dtsx.astra.cli.core.CliConstants.$Token;
import com.dtsx.astra.cli.core.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.exceptions.internal.misc.InvalidTokenException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.SetupOperation;
import com.dtsx.astra.cli.operations.SetupOperation.*;
import com.dtsx.astra.cli.utils.StringUtils;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.INVALID_TOKEN;
import static com.dtsx.astra.cli.core.output.ExitCode.UNSUPPORTED_EXECUTION;
import static com.dtsx.astra.cli.utils.StringUtils.*;

// TODO mention how to setup autocomplete
@Command(
    name = "setup",
    description = {
        "Interactively set up the Astra CLI and create profiles",
        "",
        "See @|code @{cli.name} config create|@ for a programmatic way to create profiles without interaction."
    }
)
@Example(
    comment = "Start the interactive setup process",
    command = "${cli.name} setup"
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
            case SameProfileAlreadyExists pe -> handleSameProfileAlreadyExists(pe);
            case InvalidToken(var hint) -> throwInvalidToken(hint);
        };
    }

    @Override
    protected final OutputAll execute(Supplier<SetupResult> result) {
        throw new AstraCliException(UNSUPPORTED_EXECUTION, """
          @|bold,red Error: This operation does not support outputting in the '|@@|bold,red,italic %s|@@|bold,red ' format.|@
        
          The @'!astra setup!@ command is an interactive setup command, meant to help guide you through the setup process.
        
          Use the @'!astra config create!@ command to programmatically create profiles instead.
        """, List.of(
            new Hint("Programmatically create profiles", "${cli.name} config create [name] --token <token> [--env <env>] [--default]")
        ));
    }

    private OutputHuman handleProfileCreated(ProfileCreated result) {
        val creationMessage = NL + ((result.overwritten())
            ? "@|bold Profile|@ %s @|bold successfully overwritten.|@".formatted(ctx.highlight(result.profileName()))
            : "@|bold Profile|@ %s @|bold successfully created.|@".formatted(ctx.highlight(result.profileName())));

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

    private OutputHuman handleSameProfileAlreadyExists(SameProfileAlreadyExists result) {
        return OutputHuman.response("""
          @|bold Profile|@ %s @|bold already exists with the same token and environment.|@
        
          No changes were made to your configuration.
        """.formatted(ctx.highlight(result.profileName())), List.of(
            new Hint("Manage your profiles", "${cli.name} config list"),
            new Hint("Get more information about this profile", "${cli.name} config get " + result.profileName())
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
    protected Operation<SetupResult> mkOperation() {
        return new SetupOperation(
            ctx,
            ctx.gateways()::mkOrgGateway,
            ctx.gateways().mkOrgGatewayStateless(),
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

    private void assertShouldSetup(Path existing) {
        ctx.log().banner();

        if (ctx.isNotTty()) {
            throw new AstraCliException(UNSUPPORTED_EXECUTION, """
              @|bold,red Error: Cannot run setup in non-interactive mode.|@
            
              The setup process requires user interaction, but no console is available.
            
              Please use @'!${cli.name} config create!@ to programmatically create profiles instead.
            """);
        } else {
            assert ctx.console().getConsole() != null; // not necessary, just makes the linter shut up
        }

        val prompt = """
          @|bold Welcome to the Astra CLI setup!|@
        
          @|faint A configuration file with your profile will be created at|@ @|faint,italic %s|@
        
          If you'd prefer to provide credentials on a per-command basis rather than storing them in a file, you can either:
          - Use the per-command @'!--token!@ flag to pass your existing @!AstraCS!@ token directly.
          - Use the per-command @'!--config-file!@ flag to specify an existing @!.astrarc!@ file.
        
          %s
          %s
        """.formatted(
            existing,
            renderComment(ctx.colors(), "Example:"),
            renderCommand(ctx.colors(), "${cli.name} db list --token <your_token>")
        );

        ctx.console().println(trimIndent(prompt));
        ctx.console().println();
        ctx.console().unsafeReadLine(ctx.colors().format("Press @!Enter!@ to continue, or use @!Ctrl+C!@ to cancel. "), false);
        ctx.console().println();
    }

    private void assertShouldContinueIfAlreadySetup(Path existing) {
        ctx.log().banner();

        val prompt = """
          @|bold Looks like you're already set up!|@
        
          @|faint Your config file already exists at|@ @|faint,italic %s|@
        
          Hint: You can use the @'!${cli.name} config!@ commands to manage your profiles.
       
          %s
          %s
        
          Do you want to continue and create a new profile?
        """.formatted(
            existing,
            renderComment(ctx.colors(), "Example:"),
            renderCommand(ctx.colors(), "${cli.name} config list")
        );

        val shouldContinue = ctx.console().confirm(prompt)
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
        
          You can use @'!${cli.name} config get %s!@ to get more information about the existing profile.
        
          Do you wish to overwrite it?
        """.formatted(
            ctx.highlight(existing.token().toString()),
            ctx.highlight(existing.env().name().toLowerCase()),
            existing.name().orElseThrow()
        );

        val shouldOverwrite = ctx.console().confirm(prompt)
            .defaultNo()
            .fallbackFlag("")
            .fix(List.of(), "")
            .clearAfterSelection();

        if (!shouldOverwrite) {
            throw new ExecutionCancelledException();
        }
    }

    private AstraToken promptForToken() {
        val prompt = ctx.colors().PURPLE_300.use("(Required)") + " Enter your Astra token (it should start with @!AstraCS!@)";

        return ctx.console().prompt(prompt)
            .mapper(input -> AstraToken.parse(input).getRight(InvalidTokenException::new))
            .echoOff((s) -> StringUtils.maskToken(ctx.colors(), s))
            .requireAnswer()
            .fallbackFlag("--token")
            .fix(originalArgs(), "--token <your_token>")
            .dontClearAfterSelection();
    }

    private AstraEnvironment promptForEnv(AstraEnvironment defaultEnv) {
        val prompt = ctx.colors().PURPLE_300.use("(Optional)") + " Enter the target Astra environment (defaults to @!prod!@)";

        return ctx.console().select(prompt)
            .options(AstraEnvironment.values())
            .defaultOption(defaultEnv)
            .mapper(e -> e.name().toLowerCase())
            .fallbackFlag("--env")
            .fix(originalArgs(), "--env <prod|test|dev>")
            .dontClearAfterSelection();
    }

    private ProfileName promptForName(String defaultName, AstraEnvironment env) {
        val envAddendum = (env != AstraEnvironment.PROD)
            ? " " + ctx.highlight(env.name().toLowerCase())
            : "";

        val prompt = ctx.colors().PURPLE_300.use("(Optional)") + " Enter a name for your profile (defaults to your" + envAddendum + " org name)";

        return ctx.console().prompt(prompt)
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

        return ctx.console().confirm(prompt)
            .defaultNo()
            .fallbackFlag("")
            .fix(List.of(), "")
            .clearAfterSelection();
    }
}
