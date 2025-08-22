package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.core.output.ExitCode.PROFILE_NOT_FOUND;

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
        public AstraToken token;

        @Option(names = { "--env" }, completionCandidates = AstraEnvCompletion.class, description = "Override the target astra environment", paramLabel = "ENV")
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

        val config = (credsProvider != null && credsProvider.config != null)
            ? AstraConfig.readAstraConfigFile(credsProvider.config.configFile.orElse(null), false)
            : AstraConfig.readAstraConfigFile(null, false);

        val profile = config.lookupProfile(profileName);

        if (profile.isEmpty()) {
            val filePath = config.backingFile().getAbsolutePath();
            val isDefaultConfigFile = filePath.equals(AstraConfig.resolveDefaultAstraConfigFile().getAbsolutePath());

            if (config.getValidatedProfiles().isEmpty()) {
                val hints = (isDefaultConfigFile)
                    ? List.of(
                        new Hint("Interactively create a new profile", "${cli.name} setup"),
                        new Hint("Programmatically create a new profile", "${cli.name} config create <name> --token <token> [--env <env>]")
                    )
                    : List.of(
                        new Hint("Interactively create a new profile (default config file only)", "${cli.name} setup"),
                        new Hint("Programmatically create a new profile", "${cli.name} config create <name> --token <token> [--env <env>] -cf " + config.backingFile().getPath())
                    );

                throw new AstraCliException(PROFILE_NOT_FOUND, """
                  @|bold,red Error: No profiles exist in your .astrarc file.|@
                
                  > Using configuration file at %s
                """.formatted(
                    highlight(filePath)
                ), hints);
            }

            if (profileName.isDefault()) {
                throw new AstraCliException(PROFILE_NOT_FOUND, """
                  @|bold,red Error: No default profile exists in your .astrarc file.|@
          
                  > Using configuration file at %s
                """.formatted(
                    highlight(filePath)
                ), List.of(
                    new Hint("Set a profile as default:", "${cli.name} config use" + (isDefaultConfigFile ? "" : " -cf " + config.backingFile().getPath())),
                    new Hint("Specify a profile to use:", originalArgs(), "--profile <name>")
                ));
            } else {
                throw new AstraCliException(PROFILE_NOT_FOUND, """
                  @|bold,red Error: Profile '%s' does not exist in your .astrarc file.|@
               
                  > Using configuration file at %s
                """.formatted(
                    profileName.unwrap(),
                    highlight(filePath)
                ), List.of(
                    new Hint("List available profiles:", "${cli.name} config list" + (isDefaultConfigFile ? "" : " -cf " + config.backingFile().getPath()))
                ));
            }
        }

        return cachedProfile = profile.get();
    }

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        downloadsGateway = DownloadsGateway.mkDefault(profile().token(), profile().env());
    }
}
