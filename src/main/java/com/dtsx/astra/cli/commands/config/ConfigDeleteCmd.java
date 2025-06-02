package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.output.output.OutputAll;
import com.dtsx.astra.cli.output.output.OutputHuman;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

@Command(
    name = "delete",
    aliases = { "rm" }
)
public class ConfigDeleteCmd extends AbstractCmd {
    @Parameters(completionCandidates = AvailableProfilesCompletion.class, description = "Name of the profile to delete", paramLabel = "<profile>")
    private ProfileName profileName;

    @Option(names = { "-f", "--force" }, description = "Do not fail if profile does not exist")
    private boolean force;

    @Override
    public OutputAll execute() {
        val config = config();
        
        if (config.lookupProfile(profileName).isEmpty()) {
            if (!force) {
                throw new ParameterException(spec.commandLine(), "Profile '" + profileName + "' not found");
            }
            return OutputAll.message("Profile " + AstraColors.BLUE_300.use(profileName.unwrap()) + " does not exist; nothing to delete");
        }

        config.deleteProfile(profileName);
        
        return OutputAll.message("Profile " + AstraColors.BLUE_300.use(profileName.unwrap()) + " deleted successfully");
    }
}
