package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.completions.CompletionsCache;
import com.dtsx.astra.cli.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.output.AstraConsole;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.*;

@Command(
    name = "use"
)
public class ConfigUseCmd extends AbstractCmd {
    @Parameters(description = "Profile name to set as default", completionCandidates = AvailableProfilesCompletion.class)
    private String profileName;

    @ArgGroup
    private @Nullable UniqueDefaultBehavior uniqueDefaultBehavior;

    static class UniqueDefaultBehavior {
        @Option(names = { "-f", "--force" }, description = "Force override even if default is unique")
        boolean force;

        @Option(names = { "-F", "--fail-if-unique-default" }, description = "Fail if default profile has unique configuration")
        boolean failIfUniqueDefault;
    }

    @Override
    public String executeHuman() {
        val targetProfile = config().lookupProfile(profileName)
            .orElseThrow(() -> new ParameterException(spec.commandLine(), "Profile '" + profileName + "' not found"));

        if (defaultProfileIsUnique()) {
            assertCanOverwriteDefaultProfile();
            config().deleteProfile("default");
        }

        config().createProfile("default", targetProfile.token(), targetProfile.env());

        return "Default profile set to '" + profileName + "'";
    }

    private boolean defaultProfileIsUnique() {
        val defaultProfile = config().lookupProfile("default");

        return defaultProfile.isPresent() && config().getProfiles().stream()
            .filter(p -> !p.name().equals("default"))
            .noneMatch(p -> p.token().equals(defaultProfile.get().token()) && p.env().equals(defaultProfile.get().env()));
    }

    private void assertCanOverwriteDefaultProfile() {
        if (uniqueDefaultBehavior != null && uniqueDefaultBehavior.force) {
            return;
        }

        if (uniqueDefaultBehavior != null && uniqueDefaultBehavior.failIfUniqueDefault) {
            throw new ExecutionException(spec.commandLine(), "Current default profile has unique configuration and --fail-if-unique-default was specified");
        }

        if (!AstraConsole.confirm("Current default profile has unique token/environment configuration that will be lost. Continue? [y/N] ", false)) {
            throw new ExecutionException(spec.commandLine(), "Operation cancelled. Use --force to override or save the current default as a named profile first");
        }
    }
}
