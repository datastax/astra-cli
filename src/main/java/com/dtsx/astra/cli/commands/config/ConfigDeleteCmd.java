package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.config.ConfigCreateOperation;
import com.dtsx.astra.cli.operations.config.ConfigDeleteOperation;
import com.dtsx.astra.cli.operations.config.ConfigDeleteOperation.ProfileDeleted;
import com.dtsx.astra.cli.operations.config.ConfigDeleteOperation.ProfileDoesNotExist;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "delete",
    aliases = { "rm" }
)
public class ConfigDeleteCmd extends AbstractCmd {
    @Parameters(completionCandidates = AvailableProfilesCompletion.class, description = "Name of the profile to delete", paramLabel = "<profile>")
    private ProfileName profileName;

    @Option(names = { "-f", "--force" }, description = "Do not fail if profile does not exist")
    private boolean force;

    private ConfigDeleteOperation operation;

    @Override
    public void prelude() {
        super.prelude();
        operation = new ConfigDeleteOperation(config());
    }

    @Override
    public OutputAll execute() {
        val result = operation.execute(profileName, force);

        return switch (result) {
            case ProfileDoesNotExist _ -> OutputAll.message(
                "Profile " + highlight(profileName) + " does not exist; nothing to delete"
            );
            case ProfileDeleted _ -> OutputAll.message(
                "Profile " + highlight(profileName) + " deleted successfully"
            );
        };
    }
}
