package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource.CustomFile;
import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource.DefaultFile;
import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource.Forced;
import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource.FromArgs;
import com.dtsx.astra.cli.core.CliConstants.$ConfigFile;
import com.dtsx.astra.cli.core.CliConstants.$Env;
import com.dtsx.astra.cli.core.CliConstants.$Profile;
import com.dtsx.astra.cli.core.CliConstants.$Token;
import com.dtsx.astra.cli.core.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import org.graalvm.collections.Pair;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.core.output.ExitCode.PROFILE_NOT_FOUND;

public abstract class AbstractConnectedCmd<OpRes> extends AbstractCmd<OpRes> {
    @ArgGroup(heading = "%nConnection Options:%n", order = 100)
    private @Nullable CredsProvider $credsProvider;

    public static class CredsProvider {
        @ArgGroup(exclusive = false)
        public @Nullable ConfigSpec $config;

        @ArgGroup(exclusive = false)
        public @Nullable CredsSpec $creds;
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
        private Optional<AstraEnvironment> $env;
    }

    public static class ConfigSpec {
        @Option(
            names = { $ConfigFile.LONG, $ConfigFile.SHORT },
            description = { "The @|code .astrarc|@ file to use for this command", SHOW_CUSTOM_DEFAULT + "${cli.rc-file.path}" },
            paramLabel = $ConfigFile.LABEL
        )
        private Optional<Path> $configFile;

        @Option(
            names = { $Profile.LONG, $Profile.SHORT },
            completionCandidates = AvailableProfilesCompletion.class,
            description = "The @|code .astrarc|@ profile to use for this command",
            paramLabel = $Profile.LABEL
        )
        public Optional<ProfileName> $profileName;
    }

    private @Nullable ProfileSource cachedProfileSource;
    private @Nullable Profile cachedProfile;

    public sealed interface ProfileSource {
        record Forced(Profile profile) implements ProfileSource {}
        record FromArgs(AstraToken token, Optional<AstraEnvironment> env) implements ProfileSource {}
        record CustomFile(Path path, ProfileName profile) implements ProfileSource {}
        record DefaultFile(ProfileName profile) implements ProfileSource {}
    }

    public final Profile profile() {
        if (cachedProfile != null) {
            return cachedProfile;
        }

        return cachedProfile = switch (cachedProfileSource = profileSource()) {
            case Forced(var profile) -> profile;
            case FromArgs(var token, var env) -> new Profile(Optional.empty(), token, env.orElse(AstraEnvironment.PROD), Optional.empty());
            case CustomFile(var path, var profileName) -> resolveProfileFromConfigFile(path, profileName);
            case DefaultFile(var profileName) -> resolveProfileFromConfigFile(null, profileName);
        };
    }

    public final Pair<Profile, ProfileSource> profileAndSource() {
        return Pair.create(profile(), profileSource());
    }

    private ProfileSource profileSource() {
        if (cachedProfileSource != null) {
            return cachedProfileSource;
        }

        if (ctx.forceProfileForTesting().isPresent()) {
            return new Forced(ctx.forceProfileForTesting().get());
        }

        if ($credsProvider != null && $credsProvider.$creds != null) {
            return new FromArgs($credsProvider.$creds.$token, $credsProvider.$creds.$env);
        }

        val defaultFilePath = AstraConfig.resolveDefaultAstraConfigFile(ctx);

        if ($credsProvider != null && $credsProvider.$config != null) {
            val configFile = $credsProvider.$config.$configFile;
            val profileName = $credsProvider.$config.$profileName.orElse(ProfileName.DEFAULT);

            // the check for if it's the default file is later used to decide whether to update the completions cache or not
            if (configFile.isPresent()) {
                try {
                    return (configFile.get().toRealPath().equals(defaultFilePath.toRealPath()))
                        ? new DefaultFile(profileName)
                        : new CustomFile(configFile.get(), profileName);
                } catch (Exception e) {
                    ctx.log().exception("Error resolving real file paths for getting the config file", e);
                    return new CustomFile(configFile.get(), profileName);
                }
            }

            return new DefaultFile(profileName);
        }

        return new DefaultFile(ProfileName.DEFAULT);
    }

    private Profile resolveProfileFromConfigFile(@Nullable Path path, ProfileName targetProfileName) {
        val config = AstraConfig.readAstraConfigFile(ctx, path, false);

        val profile = config.lookupProfile(targetProfileName);

        if (profile.isEmpty()) {
            val filePath = config.backingFile();
            val isDefaultConfigFile = filePath.equals(AstraConfig.resolveDefaultAstraConfigFile(ctx));

            if (config.profilesValidated().isEmpty()) {
                val hints = (isDefaultConfigFile)
                    ? List.of(
                        new Hint("Interactively create a new profile", "${cli.name} setup"),
                        new Hint("Programmatically create a new profile", "${cli.name} config create <name> --token <token> [--env <env>]")
                    )
                    : List.of(
                        new Hint("Interactively create a new profile (default config file only)", "${cli.name} setup"),
                        new Hint("Programmatically create a new profile", "${cli.name} config create <name> --token <token> [--env <env>] -cf " + config.backingFile())
                    );

                throw new AstraCliException(PROFILE_NOT_FOUND, """
                  @|bold,red Error: No profiles exist in your .astrarc file.|@
                
                  > Using configuration file at @'!%s!@
                """.formatted(
                    ctx.highlight(filePath)
                ), hints);
            }

            if (targetProfileName.isDefault()) {
                val MAX_PROFILES_IN_HINT = 3;

                var profileNames = config.profilesValidated().stream()
                    .filter(p -> p.name().isPresent())
                    .map(p -> p.name().get().unwrap())
                    .limit(MAX_PROFILES_IN_HINT)
                    .toList();

                if (config.profilesValidated().size() > MAX_PROFILES_IN_HINT) {
                    profileNames = new ArrayList<>(profileNames);
                    profileNames.add("...");
                }

                val profileNamesHint = profileNames.stream()
                    .map(n -> "'" + n + "'")
                    .reduce((a, b) -> a + "|" + b)
                    .orElse("<name>");

                val useProfileNameHint = (profileNames.size() == 1)
                    ? " " + profileNamesHint
                    : "";

                throw new AstraCliException(PROFILE_NOT_FOUND, """
                  @|bold,red Error: No default profile exists in your .astrarc file.|@
                
                  > Using configuration file at %s
                """.formatted(
                    ctx.highlight(filePath)
                ), List.of(
                    new Hint("Set a profile as default:", "${cli.name} config use" + useProfileNameHint + (isDefaultConfigFile ? "" : " -cf " + config.backingFile())),
                    new Hint("Specify a profile to use:", originalArgs(), "--profile " + profileNamesHint)
                ));
            } else {
                throw new AstraCliException(PROFILE_NOT_FOUND, """
                  @|bold,red Error: Profile '%s' does not exist in your .astrarc file.|@
                
                  > Using configuration file at %s
                """.formatted(
                    targetProfileName.unwrap(),
                    ctx.highlight(filePath)
                ), List.of(
                    new Hint("List available profiles:", "${cli.name} config list" + (isDefaultConfigFile ? "" : " -cf " + config.backingFile()))
                ));
            }
        }

        return profile.get();
    }
}
