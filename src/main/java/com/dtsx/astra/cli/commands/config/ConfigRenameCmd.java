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
import com.dtsx.astra.cli.operations.config.ConfigRenameOperation;
import com.dtsx.astra.cli.operations.config.ConfigRenameOperation.*;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.CONFIG_ALREADY_EXISTS;
import static com.dtsx.astra.cli.core.output.ExitCode.PROFILE_NOT_FOUND;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "rename",
    description = "Rename an existing configuration profile."
)
@Example(
    comment = "Rename a profile from 'old_name' to 'new_name'",
    command = "${cli.name} config rename old_name new_name"
)
@Example(
    comment = "Interactively rename a profile",
    command = "${cli.name} config rename"
)
public class ConfigRenameCmd extends AbstractConfigCmd<ConfigRenameResult> {
    @Parameters(
        index = "0",
        arity = "0..1",
        description = "Name of the profile to rename",
        completionCandidates = AvailableProfilesCompletion.class,
        paramLabel = $Profile.LABEL
    )
    public Optional<ProfileName> $oldProfileName;

    @Parameters(
        index = "1",
        arity = "1",
        description = "New name for the profile",
        paramLabel = $Profile.LABEL
    )
    public ProfileName $newProfileName;

    @Override
    public final OutputAll execute(Supplier<ConfigRenameResult> result) {
        return switch (result.get()) {
            case ProfileRenamed(var oldName, var newName) -> handleProfileRenamed(oldName, newName);
            case OldProfileNotFound(var oldName) -> throwOldProfileNotFound(oldName);
            case NewProfileAlreadyExists(var oldName, var newName) -> throwNewProfileAlreadyExists(oldName, newName);
        };
    }

    private OutputAll handleProfileRenamed(ProfileName oldName, ProfileName newName) {
        val message = "Profile %s successfully renamed to %s.".formatted(
            ctx.highlight(oldName),
            ctx.highlight(newName)
        );

        return OutputAll.response(message, mkData(oldName, newName));
    }

    private <T> T throwOldProfileNotFound(ProfileName oldName) {
        throw new AstraCliException(PROFILE_NOT_FOUND, """
          @|bold,red Error: A profile with the name '%s' could not be found.|@
        """.formatted(oldName), List.of(
            new Hint("See your existing profiles:", "${cli.name} config list")
        ));
    }

    private <T> T throwNewProfileAlreadyExists(ProfileName oldName, ProfileName newName) {
        throw new AstraCliException(CONFIG_ALREADY_EXISTS, """
          @|bold,red Error: A profile with the name '%s' already exists.|@

          Cannot rename profile %s to %s because a profile with that name already exists.
        """.formatted(
            newName,
            ctx.highlight(oldName),
            ctx.highlight(newName)
        ), List.of(
            new Hint("Delete the existing profile first:", "${cli.name} config delete " + newName),
            new Hint("See your existing profiles:", "${cli.name} config list")
        ));
    }

    private LinkedHashMap<String, Object> mkData(ProfileName oldName, ProfileName newName) {
        return sequencedMapOf(
            "oldProfileName", oldName,
            "newProfileName", newName
        );
    }

    @Override
    protected Operation<ConfigRenameResult> mkOperation() {
        val oldName = $oldProfileName.orElseGet(this::promptForOldProfileName);

        return new ConfigRenameOperation(config(false), new RenameConfigRequest(oldName, $newProfileName));
    }

    private ProfileName promptForOldProfileName() {
        val selected = ProfileNamePrompter.prompt(
            ctx,
            config(false).profiles(),
            "Select a profile to rename",
            (list) -> list,
            (b) -> b.fallbackIndex(0).fix(originalArgs(), "<old_profile>")
        );

        return ProfileName.mkUnsafe(selected);
    }
}
