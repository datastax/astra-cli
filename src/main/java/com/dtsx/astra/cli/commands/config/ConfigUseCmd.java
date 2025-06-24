package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.exceptions.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.exceptions.config.ProfileNotFoundException;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.UseConfigRequest;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Optional;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.config.ConfigUseOperation.*;

@Command(
    name = "use"
)
public class ConfigUseCmd extends AbstractCmd<ConfigUseResult> {
    @Parameters(description = "Profile name to set as default", completionCandidates = AvailableProfilesCompletion.class, paramLabel = "<profile>")
    public ProfileName profileName;

    @ArgGroup
    public @Nullable UniqueDefaultBehavior uniqueDefaultBehavior;

    public static class UniqueDefaultBehavior {
        @Option(names = { "-y", "--yes" }, description = "Force setting default profile without confirmation prompts")
        public boolean force;

        @Option(names = { "-F", "--fail-if-unique-default" }, description = "Fail if default profile has unique configuration")
        public boolean failIfUniqueDefault;
    }

    @Override
    public final OutputAll execute(ConfigUseResult result) {
        val message = switch (result) {
            case ProfileSetAsDefault() -> "Default profile set to " + highlight(profileName);
            case ProfileNotFound() -> throw new ProfileNotFoundException(profileName);
        };

        return OutputAll.message(message);
    }

    @Override
    protected Operation<ConfigUseResult> mkOperation() {
        return new ConfigUseOperation(config(), new UseConfigRequest(
            profileName,
            Optional.ofNullable(uniqueDefaultBehavior).map(ub -> ub.force).orElse(false),
            Optional.ofNullable(uniqueDefaultBehavior).map(ub -> ub.failIfUniqueDefault).orElse(false),
            this::assertCanOverwriteDefaultProfile
        ));
    }

    private void assertCanOverwriteDefaultProfile() {
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
