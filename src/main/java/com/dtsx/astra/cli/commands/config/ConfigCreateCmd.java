package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.core.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.config.ConfigCreateOperation;
import com.dtsx.astra.cli.operations.config.ConfigCreateOperation.*;
import com.dtsx.astra.cli.utils.StringUtils;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.*;

import java.util.Optional;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "create"
)
public final class ConfigCreateCmd extends AbstractCmd {
    @Parameters(arity = "0..1", description = "Profile name (defaults to organization name if not specified)", paramLabel = "<profile>")
    public Optional<ProfileName> profileName;

    public String token;

    @Option(names = { "-t", "--token" }, required = true, description = "Astra authentication token (must start with 'AstraCS:')", paramLabel = "<token>")
    protected void setToken(String token) {
        if (!token.startsWith("AstraCS:")) {
            throw new OptionValidationException("token", "Astra token should start with 'AstraCS:'");
        }
        this.token = StringUtils.removeQuotesIfAny(token);
    }

    @Option(names = { "-e", "--env" }, completionCandidates = AstraEnvCompletion.class, defaultValue = "prod", description = "Astra environment to connect to", paramLabel = "<environment>")
    public AstraEnvironment env;

    @ArgGroup
    public @Nullable ExistingProfileBehavior existingProfileBehavior;

    public static class ExistingProfileBehavior {
        @Option(names = { "-f", "--force" }, description = "Force creation without confirmation prompts")
        public boolean force;

        @Option(names = { "-F", "--fail-if-exists" }, description = "Fail if profile already exists instead of prompting")
        public boolean failIfExists;
    }

    @Override
    public OutputAll execute() {
        val operation = new ConfigCreateOperation(config());

        val result = operation.execute(new CreateConfigRequest(
            profileName,
            token,
            env,
            Optional.ofNullable(existingProfileBehavior).map(eb -> eb.force).orElse(false),
            Optional.ofNullable(existingProfileBehavior).map(eb -> eb.failIfExists).orElse(false),
            this::assertShouldSetDefaultProfile,
            this::assertCanOverwriteProfile
        ));

        return OutputAll.message(
            "Configuration %s successfully %s.".formatted(highlight(result.profileName()), (result.profileWasOverwritten()) ? "overwritten" : "created")
        );
    }

    private void assertShouldSetDefaultProfile() {
        val msg = """
            Setting the default profile directly is not recommended.

            Prefer to create a differently-named profile, and set it as the default with `astra config use`.

            Do you want to create it anyways? [y/N]""".stripIndent();

        switch (AstraConsole.confirm(msg)) {
            case ANSWER_NO -> throw new ExecutionCancelledException("Operation cancelled by user. Use --force to override.");
            case NO_ANSWER -> throw new ExecutionCancelledException("Operation cancelled due to an attempt to set the default profile without confirmation. Use --force to override.");
        }
    }

    private void assertCanOverwriteProfile(ProfileName profileName) {
        val confirmationMsg = """
            A profile under the name %s already exists in the given configuration file.

            Do you wish to overwrite it? [y/N]""".stripIndent().formatted(AstraColors.BLUE_300.use(profileName.unwrap()));

        switch (AstraConsole.confirm(confirmationMsg)) {
            case ANSWER_NO -> throw new ExecutionCancelledException("Operation cancelled by user. Use --force to override.");
            case NO_ANSWER -> throw new ExecutionCancelledException("Operation cancelled due to an attempt to overwrite an existing profile without confirmation. Use --force to override.");
        }
    }
}
