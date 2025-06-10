package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.*;

@Command(
    name = "use"
)
public class ConfigUseCmd extends AbstractCmd {
    @Parameters(description = "Profile name to set as default", completionCandidates = AvailableProfilesCompletion.class, paramLabel = "<profile>")
    private ProfileName profileName;

    @ArgGroup
    private @Nullable UniqueDefaultBehavior uniqueDefaultBehavior;

    static class UniqueDefaultBehavior {
        @Option(names = { "-f", "--force" }, description = "Force setting default profile without confirmation prompts")
        boolean force;

        @Option(names = { "-F", "--fail-if-unique-default" }, description = "Fail if default profile has unique configuration")
        boolean failIfUniqueDefault;
    }

    @Override
    public OutputAll execute() {
        val targetProfile = config().lookupProfile(profileName)
            .orElseThrow(() -> new ParameterException(spec.commandLine(), "Profile '" + profileName + "' not found"));

        if (defaultProfileIsUnique()) {
            assertCanOverwriteDefaultProfile();
            config().deleteProfile(ProfileName.DEFAULT);
        }

        config().createProfile(ProfileName.DEFAULT, targetProfile.token(), targetProfile.env());

        return OutputAll.message("Default profile set to '" + profileName + "'");
    }

    private boolean defaultProfileIsUnique() {
        val defaultProfile = config().lookupProfile(ProfileName.DEFAULT);

        return defaultProfile.isPresent() && config().getProfiles().stream()
            .filter(p -> !p.name().equals(ProfileName.DEFAULT))
            .noneMatch(p -> p.token().equals(defaultProfile.get().token()) && p.env().equals(defaultProfile.get().env()));
    }

    private void assertCanOverwriteDefaultProfile() {
        if (uniqueDefaultBehavior != null && uniqueDefaultBehavior.force) {
            return;
        }

        if (uniqueDefaultBehavior != null && uniqueDefaultBehavior.failIfUniqueDefault) {
            throw new ExecutionException(spec.commandLine(), "Current default profile has unique configuration and --fail-if-unique-default was specified");
        }

        val msg = """
            Current default profile has unique token+environment configuration that will be lost.

            It is recommended to save the current default profile as a named profile first, if the configuration is still needed.

            Do you want to continue regardless? [y/N]""".stripIndent() + " ";

        switch (AstraConsole.confirm(msg)) {
            case ANSWER_NO -> throw new ExecutionCancelledException("Operation cancelled by user. Use --force to override.");
            case NO_ANSWER -> throw new ExecutionCancelledException("Operation cancelled due to an attempt to overwrite the unique configuration in the default profile without confirmation. Use --force to override.");
        }
    }
}
