package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.exceptions.internal.config.ProfileNotFoundException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.ConfigUseResult;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.ProfileNotFound;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.ProfileSetAsDefault;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.UseConfigRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "use",
    description = "Sets an existing profile to be used as the default for all commands. Use the `--default` option when creating a new profile to set it as the default automatically."
)
@Example(
    comment = "Set an existing profile as the default",
    command = "config use my_profile"
)
@Example(
    comment = "Set a profile as the default when creating it",
    command = "config create my_profile -t @token.txt --default"
)
public class ConfigUseCmd extends AbstractConfigCmd<ConfigUseResult> {
    @Parameters(
        description = "Profile to set as default",
        completionCandidates = AvailableProfilesCompletion.class,
        paramLabel = "PROFILE"
    )
    public ProfileName $profileName;

    @Override
    public final OutputAll execute(ConfigUseResult result) {
        return switch (result) {
            case ProfileSetAsDefault() -> OutputAll.message("Default profile set to " + highlight($profileName));
            case ProfileNotFound() -> throw new ProfileNotFoundException($profileName);
        };
    }

    @Override
    protected Operation<ConfigUseResult> mkOperation() {
        return new ConfigUseOperation(config(), new UseConfigRequest($profileName));
    }
}
