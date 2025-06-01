package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.output.AstraConsole;
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
    @Parameters(arity = "0..1")
    private Optional<String> profileName;

    protected String token;

    @Option(names = { "-t", "--token" }, required = true)
    protected void setToken(String token) {
        if (!token.startsWith("AstraCS:")) {
            throw new ParameterException(spec.commandLine(), "Astra token should start with 'AstraCS:'");
        }
        this.token = StringUtils.removeQuotesIfAny(token);
    }

    @Option(names = { "-e", "--env" }, completionCandidates = AstraEnvCompletion.class, defaultValue = "prod")
    protected AstraEnvironment env;

    @ArgGroup
    private @Nullable ExistingProfileBehavior existingProfileBehavior;

    static class ExistingProfileBehavior {
        @Option(names = { "-f", "--force" })
        boolean force;

        @Option(names = { "-F", "--fail-if-exists" })
        boolean failIfExists;
    }

    @Override
    public String executeHuman() {
        val org = fetchTokenOrg(token, env);
        val profileName = getProfileName(org);

        if (profileExists(profileName)) {
            assertCanOverwriteProfile(profileName);
            config().deleteProfile(profileName);
        }

        config().createProfile(profileName, token, env);

        return "Configuration created";
    }

    private Organization fetchTokenOrg(String token, AstraEnvironment env) {
        try {
            return new AstraOpsClient(token, env).getOrganization();
        } catch (Exception e) {
            throw new ParameterException(spec.commandLine(), "Error validating your astra token" + ((env != AstraEnvironment.PROD) ? "; make sure token targets the proper environment (%s)" : ""));
        }
    }

    private String getProfileName(Organization org) {
        return StringUtils.removeQuotesIfAny(profileName.orElse(org.getName()));
    }

    private boolean profileExists(String name) {
        return config().lookupProfile(name).isPresent();
    }

    private void assertCanOverwriteProfile(String name) {
        if (existingProfileBehavior != null && existingProfileBehavior.force) {
            return;
        }

        val messageStart = "A profile under the name '" + name + "' already exists in the given config file";

        if (existingProfileBehavior != null && existingProfileBehavior.failIfExists) {
            throw new ExecutionException(spec.commandLine(), messageStart + ", and the `--fail-if-exists` flag was passed, so the profile was not created");
        }

        if (!AstraConsole.confirm(messageStart + "... do you want to override it? [y/N] ", false)) {
            throw new ExecutionException(spec.commandLine(), messageStart + ". Either interactively pass `y`, or pass the `--force` flag to bypass this check");
        }
    }
}
