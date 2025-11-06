package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.core.CliConstants.$Profile;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.prompters.specific.ProfileNamePrompter;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.ConfigUseResult;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.ProfileNotFound;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.ProfileSetAsDefault;
import com.dtsx.astra.cli.operations.config.ConfigUseOperation.UseConfigRequest;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.PROFILE_NOT_FOUND;

@Command(
    name = "use",
    description = "Sets an existing profile to be used as the default for all commands. Use the @|code --default|@ option when creating a new profile to set it as the default automatically."
)
@Example(
    comment = "Set an existing profile as the default",
    command = "${cli.name} config use my_profile"
)
@Example(
    comment = "Set a profile as the default when creating it",
    command = "${cli.name} config create my_profile -t @token.txt --default"
)
public class ConfigUseCmd extends AbstractConfigCmd<ConfigUseResult> {
    @Parameters(
        arity = "0..1",
        description = "Profile to set as default",
        completionCandidates = AvailableProfilesCompletion.class,
        paramLabel = $Profile.LABEL
    )
    public Optional<ProfileName> $profileName;

    @Override
    public final OutputAll execute(Supplier<ConfigUseResult> result) {
        return switch (result.get()) {
            case ProfileSetAsDefault(var profileName) -> OutputAll.response("Default profile set to " + ctx.highlight(profileName));
            case ProfileNotFound(var profileName) -> throwProfileNotFound(profileName);
        };
    }

    private <T> T throwProfileNotFound(ProfileName profileName) {
        throw new AstraCliException(PROFILE_NOT_FOUND, """
          @|bold,red Error: A profile with the name '%s' could not be found.|@
        """.formatted(profileName), List.of(
            new Hint("See your existing profiles", "${cli.name} config list")
        ));
    }

    @Override
    protected Operation<ConfigUseResult> mkOperation() {
        return new ConfigUseOperation(config(false), new UseConfigRequest($profileName.orElseGet(this::promptForProfileName)));
    }

    private ProfileName promptForProfileName() {
        val selected = ProfileNamePrompter.prompt(ctx, config(false).profiles(), "Select a profile to set as default",
            (list) -> {
                val onlyValid = NEList.parse(list.stream().filter(Either::isRight).toList());

                if (onlyValid.isEmpty()) {
                    throw new AstraCliException(PROFILE_NOT_FOUND, """
                      @|bold,red No valid profiles found to select from|@
                    """);
                }

                val notDefault = NEList.parse(onlyValid.get().stream().filter(e -> !e.getRight().isDefault()).toList());

                if (notDefault.isEmpty()) {
                    throw new AstraCliException(PROFILE_NOT_FOUND, """
                      @|bold,red No other (non-default) profiles found to select from|@
                    """);
                }

                return notDefault.get();
            },
            (b) -> b.fallbackIndex(0).fix(originalArgs(), "<profile>")
        );

        return ProfileName.mkUnsafe(selected);
    }
}
