package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.exceptions.db.ExecutionCancelledException;
import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.output.AstraConsole;
import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.cli.output.output.OutputAll;
import com.dtsx.astra.cli.utils.StringUtils;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.*;

import java.util.Optional;

@Command(
    name = "create"
)
public class ConfigCreateCmd extends AbstractCmd {
    @Parameters(arity = "0..1", description = "Profile name (defaults to organization name if not specified)", paramLabel = "<profile>")
    private Optional<ProfileName> profileName;

    protected String token;

    @Option(names = { "-t", "--token" }, required = true, description = "Astra authentication token (must start with 'AstraCS:')", paramLabel = "<token>")
    protected void setToken(String token) {
        if (!token.startsWith("AstraCS:")) {
            throw new ParameterException(spec.commandLine(), "Astra token should start with 'AstraCS:'");
        }
        this.token = StringUtils.removeQuotesIfAny(token);
    }

    @Option(names = { "-e", "--env" }, completionCandidates = AstraEnvCompletion.class, defaultValue = "prod", description = "Astra environment to connect to", paramLabel = "<environment>")
    protected AstraEnvironment env;

    @ArgGroup
    private @Nullable ExistingProfileBehavior existingProfileBehavior;

    static class ExistingProfileBehavior {
        @Option(names = { "-f", "--force" }, description = "Force creation without confirmation prompts")
        boolean force;

        @Option(names = { "-F", "--fail-if-exists" }, description = "Fail if profile already exists instead of prompting")
        boolean failIfExists;
    }

    @Override
    public OutputAll execute() {
        val org = fetchTokenOrg(token, env);
        val profileName = getProfileName(org);

        if (profileName.equals(ProfileName.DEFAULT)) {
            assertShouldSetDefaultProfile();
        }

        val profileExists = profileExists(profileName);

        if (profileExists) {
            assertCanOverwriteProfile(profileName);
            config().deleteProfile(profileName);
        }

        config().createProfile(profileName, token, env);

        return OutputAll.message(
            "Configuration %s successfully %s.".formatted(AstraColors.BLUE_300.use(profileName.unwrap()), (profileExists) ? "overwritten" : "created")
        );
    }

    private Organization fetchTokenOrg(String token, AstraEnvironment env) {
        try {
            return AstraLogger.loading("Validating your Astra token...", null, (_) -> (
                new AstraOpsClient(token, env).getOrganization()
            ));
        } catch (Exception e) {
            throw new ExecutionCancelledException("Error validating your astra token" + ((env != AstraEnvironment.PROD) ? "; make sure token targets the proper environment (%s)" : ""));
        }
    }

    private ProfileName getProfileName(Organization org) {
        return profileName.orElse(ProfileName.mkUnsafe(org.getName()));
    }

    private boolean profileExists(ProfileName name) {
        return config().lookupProfile(name).isPresent();
    }

    private void assertShouldSetDefaultProfile() {
        if (existingProfileBehavior != null && existingProfileBehavior.force) {
            return;
        }

        val msg = """
            Setting the default profile directly is not recommended.

            Prefer to create a differently-named profile, and set it as the default with `astra config use`.

            Do you want to create it anyways? [y/N]""".stripIndent() + " ";

        switch (AstraConsole.confirm(msg)) {
            case ANSWER_NO -> throw new ExecutionCancelledException("Operation cancelled by user. Use --force to override.");
            case NO_ANSWER -> throw new ExecutionCancelledException("Operation cancelled due to an attempt to set the default profile without confirmation. Use --force to override.");
        }
    }

    private void assertCanOverwriteProfile(ProfileName profileName) {
        if (existingProfileBehavior != null && existingProfileBehavior.force) {
            return;
        }

        if (existingProfileBehavior != null && existingProfileBehavior.failIfExists) {
            throw new ExecutionCancelledException("Operation cancelled because to profile '%s' already exists, and --fail-if-exists was present.".formatted(profileName));
        }

        val confirmationMsg = """
            A profile under the name %s already exists in the given configuration file.

            Do you wish to overwrite it? [y/N]""".stripIndent().formatted(AstraColors.BLUE_300.use(profileName.unwrap())) + " ";

        switch (AstraConsole.confirm(confirmationMsg)) {
            case ANSWER_NO -> throw new ExecutionCancelledException("Operation cancelled by user. Use --force to override.");
            case NO_ANSWER -> throw new ExecutionCancelledException("Operation cancelled due to an attempt to overwrite an existing profile without confirmation. Use --force to override.");
        }
    }
}
