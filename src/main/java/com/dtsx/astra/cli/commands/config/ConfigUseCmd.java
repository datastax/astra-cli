package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.internal.config.ProfileNotFoundException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.ConfigUseResult;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.ProfileNotFound;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.ProfileSetAsDefault;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.UseConfigRequest;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
        arity = "0..1",
        description = "Profile to set as default",
        completionCandidates = AvailableProfilesCompletion.class,
        paramLabel = "PROFILE"
    )
    public Optional<ProfileName> $profileName;

    @Override
    public final OutputAll execute(Supplier<ConfigUseResult> result) {
        return switch (result.get()) {
            case ProfileSetAsDefault(var profileName) -> OutputAll.message("Default profile set to " + highlight(profileName));
            case ProfileNotFound(var profileName) -> throw new ProfileNotFoundException(profileName);
        };
    }

    @Override
    protected Operation<ConfigUseResult> mkOperation() {
        return new ConfigUseOperation(config(false), new UseConfigRequest($profileName, this::promptForProfile));
    }

    private AstraConfig.Profile promptForProfile(Optional<Profile> defaultProfile, NEList<Profile> candidates) {
        val maxNameLength = candidates.stream()
            .map(p -> p.nameOrDefault().unwrap().length())
            .max(Integer::compareTo)
            .orElse(0);

        val profileToDisplayMap = candidates.stream()
            .collect(Collectors.toMap(
                p -> p,
                p -> {
                    val name = p.nameOrDefault().unwrap();

                    val tags = new ArrayList<String>() {{
                        add(p.env().name().toLowerCase());

                        if (defaultProfile.isPresent() && defaultProfile.get().token().equals(p.token()) && defaultProfile.get().env().equals(p.env())) {
                            add("already default");
                        }
                    }};

                    val tagsAsString = (!tags.isEmpty())
                        ? AstraColors.NEUTRAL_500.use(" (" + String.join(", ", tags) + ")")
                        : "";

                    return name + " ".repeat(maxNameLength - name.length()) + tagsAsString;
                }
            ));

        return AstraConsole.select("Select a profile to set as default")
            .options(candidates)
            .requireAnswer()
            .mapper(profileToDisplayMap::get)
            .fallbackIndex(0)
            .fix(originalArgs(), "<profile>")
            .clearAfterSelection();
    }
}
