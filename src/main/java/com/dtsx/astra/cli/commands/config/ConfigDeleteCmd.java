package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigDeleteOperation;
import com.dtsx.astra.cli.operations.config.ConfigDeleteOperation.*;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.PROFILE_NOT_FOUND;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "delete",
    description = "Delete an existing configuration profile."
)
@Example(
    comment = "Delete a profile named 'my_profile'",
    command = "astra config delete my_profile"
)
@Example(
    comment = "Delete a profile named 'my_profile' without failing if it does not exist",
    command = "astra config delete my_profile --if-exists"
)
public class ConfigDeleteCmd extends AbstractConfigCmd<ConfigDeleteResult> {
    @Parameters(
        description = "Name of the profile to delete",
        completionCandidates = AvailableProfilesCompletion.class,
        paramLabel = "PROFILE"
    )
    public ProfileName $profileName;

    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if the profile does not exist", DEFAULT_VALUE }
    )
    public boolean $ifExists;

    @Override
    public final OutputAll execute(Supplier<ConfigDeleteResult> result) {
        return switch (result.get()) {
            case ProfileDeleted() -> handleProfileDeleted();
            case ProfileDoesNotExist() -> handleProfileDoesNotExist();
            case ProfileIllegallyDoesNotExist() -> throwProfileNotFound();
        };
    }

    private OutputAll handleProfileDeleted() {
        val message = "Profile %s deleted successfully.".formatted(highlight($profileName));

        return OutputAll.response(message, mkData(true));
    }

    private OutputAll handleProfileDoesNotExist() {
        val message = "Profile %s does not exist; nothing to delete.".formatted(highlight($profileName));

        return OutputAll.response(message, mkData(false), List.of(
            new Hint("See your existing profiles:", "astra config list")
        ));
    }

    private <T> T throwProfileNotFound() {
        throw new AstraCliException(PROFILE_NOT_FOUND, """
          @|bold,red Error: A profile with the name '%s' could not be found.|@

          To ignore this error, you can use the @!--if-exists!@ option to avoid failing if the profile does not exist.
        """.formatted(
            $profileName
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-exists"),
            new Hint("See your existing profiles:", "astra config list")
        ));
    }

    private Map<String, Object> mkData(Boolean wasDeleted) {
        return Map.of(
            "wasDeleted", wasDeleted
        );
    }

    @Override
    protected Operation<ConfigDeleteResult> mkOperation() {
        return new ConfigDeleteOperation(config(false), new CreateDeleteRequest($profileName, $ifExists));
    }
}
