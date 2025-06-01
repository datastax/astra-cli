package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.util.Optional;

public abstract class AbstractConnectedCmd extends AbstractCmd {
    @Option(names = { "-e", "--env" }, completionCandidates = AstraEnvCompletion.class)
    private Optional<AstraEnvironment> env;

    @ArgGroup
    private @Nullable TokenProvider tokenProvider;

    static class TokenProvider {
        @Option(names = { "--profile", "-p" }, completionCandidates = AvailableProfilesCompletion.class)
        private Optional<String> profileName;

        @Option(names = { "--token" })
        private Optional<String> token;
    }

    private @Nullable Profile cachedProfile;

    protected Profile profile() {
        if (cachedProfile != null) {
            return cachedProfile;
        }

        if (tokenProvider != null && tokenProvider.token.isPresent()) {
            return cachedProfile = new Profile("<faux_profile>", tokenProvider.token.get(), env.orElse(AstraEnvironment.PROD));
        }

        val profileName = (tokenProvider != null) && tokenProvider.profileName.isPresent()
            ? tokenProvider.profileName.get()
            : "default";

        return cachedProfile = config().lookupProfile(profileName)
            .orElseThrow(() -> new ParameterException(spec.commandLine(), "Profile '" + profileName + "' does not exist"));
    }
}
