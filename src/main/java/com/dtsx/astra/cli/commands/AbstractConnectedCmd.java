package com.dtsx.astra.cli.commands;

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
import org.jetbrains.annotations.MustBeInvokedByOverriders;
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
            description = "Override the default astra token",
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
            description = { "The astrarc file to use", SHOW_CUSTOM_DEFAULT + "${cli.rc-file.path}" },
            paramLabel = $ConfigFile.LABEL
        )
        private Optional<Path> $configFile;

        @Option(
            names = { $Profile.LONG, $Profile.SHORT },
            completionCandidates = AvailableProfilesCompletion.class,
            description = "The astrarc profile to use",
            paramLabel = $Profile.LABEL
        )
        public Optional<ProfileName> $profileName;
    }

    private @Nullable Profile cachedProfile;

    public final Profile profile() {
        if (cachedProfile != null) {
            return cachedProfile;
        }

        if (ctx.forceUseProfile().isPresent()) {
            return cachedProfile = ctx.forceUseProfile().get();
        }

        if ($credsProvider != null && $credsProvider.$creds != null) {
            return cachedProfile = new Profile(Optional.empty(), $credsProvider.$creds.$token, $credsProvider.$creds.$env.orElse(AstraEnvironment.PROD));
        }

        val targetProfileName = ($credsProvider != null && $credsProvider.$config != null && $credsProvider.$config.$profileName.isPresent())
            ? $credsProvider.$config.$profileName.get()
            : ProfileName.DEFAULT;

        val config = ($credsProvider != null && $credsProvider.$config != null)
            ? AstraConfig.readAstraConfigFile(ctx, $credsProvider.$config.$configFile.orElse(null), false)
            : AstraConfig.readAstraConfigFile(ctx, null, false);

        val profile = config.lookupProfile(targetProfileName);

        if (profile.isEmpty()) {
            val filePath = config.backingFile();
            val isDefaultConfigFile = filePath.equals(AstraConfig.resolveDefaultAstraConfigFile(ctx));

            if (config.getValidatedProfiles().isEmpty()) {
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

                var profileNames = config.getValidatedProfiles().stream()
                    .filter(p -> p.name().isPresent())
                    .map(p -> p.name().get().unwrap())
                    .limit(MAX_PROFILES_IN_HINT)
                    .toList();

                if (config.getValidatedProfiles().size() > MAX_PROFILES_IN_HINT) {
                    profileNames = new ArrayList<>(profileNames);
                    profileNames.add("...");
                }

                val profileNamesHint = profileNames.stream()
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

        return cachedProfile = profile.get();
    }

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
    }
}
