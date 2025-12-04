package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.CliConstants.$ConfigFile;
import com.dtsx.astra.cli.core.CliConstants.$Env;
import com.dtsx.astra.cli.core.CliConstants.$Profile;
import com.dtsx.astra.cli.core.CliConstants.$Token;
import com.dtsx.astra.cli.core.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.properties.CliProperties.ConstEnvVars;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.Optional;

@NoArgsConstructor
public class ConnectionOptions {
    public static final ConnectionOptions EMPTY = new ConnectionOptions();

    @ArgGroup(exclusive = false)
    public @Nullable ConfigSpec $config;

    @ArgGroup(exclusive = false)
    public @Nullable CredsSpec $creds;

    public ConnectionOptions(@Nullable ConfigSpec $config, @Nullable CredsSpec $creds) {
        this.$config = $config;
        this.$creds = $creds;
    }

    public static class CredsSpec {
        @Option(
            names = { $Token.LONG },
            description = "The astra token to use for this command. Use the @|code --token @file|@ syntax to read the token from a file, to avoid potential leaks.",
            paramLabel = $Token.LABEL,
            required = true
        )
        public AstraToken $token;

        @Option(
            names = { $Env.LONG },
            completionCandidates = AstraEnvCompletion.class,
            description = "Override the target astra environment",
            paramLabel = $Env.LABEL
        )
        public Optional<AstraEnvironment> $env;

        public CredsSpec(AstraToken $token, Optional<AstraEnvironment> $env) {
            this.$token = $token;
            this.$env = $env;
        }

        public CredsSpec() {
            this(null, Optional.empty());
        }
    }

    public static class ConfigSpec {
        @Option(
            names = { $ConfigFile.LONG, $ConfigFile.SHORT },
            description = { "The @|code .astrarc|@ file to use for this command", AbstractCmd.SHOW_CUSTOM_DEFAULT + "${cli.rc-file.path}" },
            paramLabel = $ConfigFile.LABEL
        )
        public Optional<Path> $configFile;

        @Option(
            names = { $Profile.LONG, $Profile.SHORT },
            completionCandidates = AvailableProfilesCompletion.class,
            description = "The @|code .astrarc|@ profile to use for this command. Can be set via @|code " + ConstEnvVars.PROFILE + "|@.",
            paramLabel = $Profile.LABEL
        )
        public Optional<ProfileName> $profileName;

        public ConfigSpec(Optional<Path> $configFile, Optional<ProfileName> $profileName) {
            this.$configFile = $configFile;
            this.$profileName = $profileName;
        }

        public ConfigSpec() {
            this(Optional.empty(), Optional.empty());
        }
    }

    public ConnectionOptions merge(ConnectionOptions other) {
        if (this == EMPTY) {
            return other;
        }
        if (other == EMPTY) {
            return this;
        }
        return new ConnectionOptions(mergeConfigSpec(other), mergeCredsSpec(other));
    }

    private @Nullable ConfigSpec mergeConfigSpec(ConnectionOptions other) {
        if (this.$config != null || other.$config != null) {
            val configFile = (other.$config != null && other.$config.$configFile != null && other.$config.$configFile.isPresent())
                ? other.$config.$configFile
                : (this.$config != null ? this.$config.$configFile : Optional.<Path>empty());

            val profileName = (other.$config != null && other.$config.$profileName != null && other.$config.$profileName.isPresent())
                ? other.$config.$profileName
                : (this.$config != null ? this.$config.$profileName : Optional.<ProfileName>empty());

            return new ConfigSpec(configFile, profileName);
        }
        return null;
    }

    private @Nullable CredsSpec mergeCredsSpec(ConnectionOptions other) {
        if (this.$creds != null || other.$creds != null) {
            val token = (other.$creds != null && other.$creds.$token != null)
                ? other.$creds.$token
                : (this.$creds != null ? this.$creds.$token : null);

            val env = (other.$creds != null && other.$creds.$env != null && other.$creds.$env.isPresent())
                ? other.$creds.$env
                : (this.$creds != null ? this.$creds.$env : Optional.<AstraEnvironment>empty());

            return new CredsSpec(token, env);
        }
        return null;
    }
}
