package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.output.AstraConsole;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.*;

@Command(
    name = "delete",
    aliases = { "rm" }
)
public class ConfigDeleteCmd extends AbstractCmd {
    @Parameters(completionCandidates = AvailableProfilesCompletion.class)
    private String profileName;

    @Option(names = { "-f", "--force" })
    private boolean force;

    @Override
    public String executeHuman() {
        val config = config();
        
        if (config.lookupProfile(profileName).isEmpty()) {
            if (!force) {
                throw new ParameterException(spec.commandLine(), "Profile '" + profileName + "' not found");
            }
            return "";
        }

        config.deleteProfile(profileName);
        
        return "Profile '" + profileName + "' deleted successfully";
    }
}
