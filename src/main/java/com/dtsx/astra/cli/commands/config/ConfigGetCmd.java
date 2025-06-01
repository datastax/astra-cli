package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.completions.impls.ProfileKeysCompletion;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

import java.util.Optional;

@Command(
    name = "get",
    aliases = { "describe" }
)
public class ConfigGetCmd extends AbstractCmd {
    @Parameters(completionCandidates = AvailableProfilesCompletion.class)
    private String profileName;

    @Option(names = { "-k", "--key" }, completionCandidates = ProfileKeysCompletion.class)
    private Optional<String> key = Optional.empty();

    @Override
    public String executeHuman() {
        try {
            val section = config().getProfileSection(profileName);

            if (key.isPresent()) {
                return section
                    .lookupKey(key.get())
                    .orElseThrow(() -> new ParameterException(spec.commandLine(), "Key '" + key.get() + "' not found in profile '" + profileName + "'"));
            } else {
                return section.write(true);
            }
        } catch (Exception e) {
            throw new ParameterException(spec.commandLine(), e.getMessage());
        }
    }
}
