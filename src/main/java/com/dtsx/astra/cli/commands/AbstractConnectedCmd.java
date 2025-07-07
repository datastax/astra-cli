package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.models.Token;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.io.File;
import java.util.Optional;

public abstract class AbstractConnectedCmd<OpRes> extends AbstractCmd<OpRes> {
    @ArgGroup
    private @Nullable CredsProvider credsProvider;

    public static class CredsProvider {
        @ArgGroup(exclusive = false)
        public @Nullable ConfigSpec config;

        @ArgGroup(exclusive = false)
        public @Nullable CredsSpec creds;
    }

    public static class CredsSpec {
        @Option(names = { "--token" }, description = "Override the default astra token", paramLabel = "TOKEN", required = true)
        public Token token;

        @Option(names = { "--env" }, completionCandidates = AstraEnvCompletion.class, description = "Override the target astra environment", paramLabel = "<environment>")
        private Optional<AstraEnvironment> env;
    }

    public static class ConfigSpec {
        @Option(names = { "--config-file", "-cf" }, description = "The astrarc file to use", paramLabel = "PATH")
        private Optional<File> configFile;

        @Option(names = { "--profile", "-p" }, completionCandidates = AvailableProfilesCompletion.class, description = "Specify the astrarc profile to use", paramLabel = "NAME")
        public Optional<ProfileName> profileName;
    }

    private @Nullable Profile cachedProfile;

    protected DownloadsGateway downloadsGateway;

    public final Profile profile() {
        if (cachedProfile != null) {
            return cachedProfile;
        }

        if (credsProvider != null && credsProvider.creds != null) {
            return cachedProfile = new Profile(Optional.empty(), credsProvider.creds.token, credsProvider.creds.env.orElse(AstraEnvironment.PROD));
        }

        val profileName = (credsProvider != null && credsProvider.config != null && credsProvider.config.profileName.isPresent())
            ? credsProvider.config.profileName.get()
            : ProfileName.DEFAULT;

        val configFile = (credsProvider != null && credsProvider.config != null)
            ? AstraConfig.readAstraConfigFile(credsProvider.config.configFile.orElse(null))
            : AstraConfig.readAstraConfigFile(null);

        return cachedProfile = configFile.lookupProfile(profileName)
            .orElseThrow(() -> new ParameterException(spec.commandLine(), "Profile '" + profileName + "' does not exist"));
    }

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        downloadsGateway = DownloadsGateway.mkDefault(profile().token(), profile().env());
    }
}
