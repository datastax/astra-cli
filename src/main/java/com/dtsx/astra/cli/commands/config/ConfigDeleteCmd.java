package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.core.CliConstants.$Profile;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.prompters.specific.ProfileNamePrompter;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigDeleteOperation;
import com.dtsx.astra.cli.operations.config.ConfigDeleteOperation.*;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.PROFILE_NOT_FOUND;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "delete",
    description = "Delete an existing configuration profile."
)
@Example(
    comment = "Delete a profile named 'my_profile'",
    command = "${cli.name} config delete my_profile"
)
@Example(
    comment = "Delete a profile named 'my_profile' without failing if it does not exist",
    command = "${cli.name} config delete my_profile --if-exists"
)
public class ConfigDeleteCmd extends AbstractConfigCmd<ConfigDeleteResult> {
    @Parameters(
        arity = "0..1",
        description = "Name of the profile to delete",
        completionCandidates = AvailableProfilesCompletion.class,
        paramLabel = $Profile.LABEL
    )
    public Optional<ProfileName> $profileName;

    @Option(
        names = { "--if-exists" },
        description = "Do not fail if the profile does not exist"
    )
    public boolean $ifExists;

    @Override
    public final OutputAll execute(Supplier<ConfigDeleteResult> result) {
        return switch (result.get()) {
            case ProfileDeleted(var profileName) -> handleProfileDeleted(profileName);
            case ProfileDoesNotExist(var profileName) -> handleProfileDoesNotExist(profileName);
            case ProfileIllegallyDoesNotExist(var profileName) -> throwProfileNotFound(profileName);
        };
    }

    private OutputAll handleProfileDeleted(ProfileName profileName) {
        val message = "Profile %s deleted successfully.".formatted(ctx.highlight(profileName));

        return OutputAll.response(message, mkData(true));
    }

    private OutputAll handleProfileDoesNotExist(ProfileName profileName) {
        val message = "Profile %s does not exist; nothing to delete.".formatted(ctx.highlight(profileName));

        return OutputAll.response(message, mkData(false), List.of(
            new Hint("See your existing profiles:", "${cli.name} config list")
        ));
    }

    private <T> T throwProfileNotFound(ProfileName profileName) {
        throw new AstraCliException(PROFILE_NOT_FOUND, """
          @|bold,red Error: A profile with the name '%s' could not be found.|@

          To ignore this error, you can use the @'!--if-exists!@ option to avoid failing if the profile does not exist.
        """.formatted(
            profileName
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-exists"),
            new Hint("See your existing profiles:", "${cli.name} config list")
        ));
    }

    private LinkedHashMap<String, Object> mkData(Boolean wasDeleted) {
        return sequencedMapOf(
            "wasDeleted", wasDeleted
        );
    }

    @Override
    protected Operation<ConfigDeleteResult> mkOperation() {
        return new ConfigDeleteOperation(config(false), new CreateDeleteRequest($profileName.orElseGet(this::promptForProfileName), $ifExists));
    }

    private ProfileName promptForProfileName() {
        val selected = ProfileNamePrompter.prompt(ctx, config(false).profiles(), "Select a profile to delete",
            (list) -> list,
            (b) -> b.fallbackIndex(0).fix(originalArgs(), "<profile>")
        );

        return ProfileName.mkUnsafe(selected);
    }
}
