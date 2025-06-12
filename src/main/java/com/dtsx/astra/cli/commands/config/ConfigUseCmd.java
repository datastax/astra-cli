package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.exceptions.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.output.OutputAll;
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
        new ConfigUseOperation(config()).execute(new UseConfigRequest(
            profileName,
            Optional.ofNullable(uniqueDefaultBehavior).map(ub -> ub.force).orElse(false),
            Optional.ofNullable(uniqueDefaultBehavior).map(ub -> ub.failIfUniqueDefault).orElse(false),
            this::assertCanOverwriteDefaultProfile
        ));

        return OutputAll.message("Default profile set to " + highlight(profileName));
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
