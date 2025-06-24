package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.config.ProfileNotFoundException;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigDeleteOperation;
import com.dtsx.astra.cli.operations.config.ConfigDeleteOperation.*;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

@Command(
    name = "delete"
)
public class ConfigDeleteCmd extends AbstractCmd<ConfigDeleteResult> {
    @Parameters(completionCandidates = AvailableProfilesCompletion.class, description = "Name of the profile to delete", paramLabel = "<profile>")
    public ProfileName profileName;

    @Option(names = { "--if-exists" }, description = "Do not fail if profile does not exist")
    public boolean ifExists;

    @Override
    public final OutputAll execute(ConfigDeleteResult result) {
        val message = switch (result) {
            case ProfileDoesNotExist() -> """
                Profile %s does not exist; nothing to delete.
                
                Use %s to list your available profiles.
                """.formatted(highlight(profileName), highlight("astra config list"));

            case ProfileIllegallyDoesNotExist() ->
                throw new ProfileNotFoundException(profileName, "; use --if-exists to ignore this error");

            case ProfileDeleted() -> """
                Profile %s deleted successfully.
                """.formatted(highlight(profileName));
        };

        return OutputAll.message(trimIndent(message));
    }

    @Override
    protected Operation<ConfigDeleteResult> mkOperation() {
        return new ConfigDeleteOperation(config(), new CreateDeleteRequest(profileName, ifExists));
    }
}
