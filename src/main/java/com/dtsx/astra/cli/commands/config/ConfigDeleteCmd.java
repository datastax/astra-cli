package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigDeleteOperation;
import com.dtsx.astra.cli.operations.config.ConfigDeleteOperation.*;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.PROFILE_NOT_FOUND;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.utils.StringUtils.*;

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
public class ConfigDeleteCmd extends AbstractCmd<ConfigDeleteResult> {
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
    public final OutputAll execute(ConfigDeleteResult result) {
        val message = switch (result) {
            case ProfileDoesNotExist() -> """
              Profile %s does not exist; nothing to delete.
            
              %s
              %s
            """.formatted(
                highlight($profileName),
                renderComment("See your existing profiles:"),
                renderCommand("astra config list")
            );

            case ProfileIllegallyDoesNotExist() -> throw new AstraCliException(PROFILE_NOT_FOUND, """
              @|bold,red Error: A profile with the name '%s' could not be found.|@

              To ignore this error, you can use the %s option to avoid failing if the profile does not exist.

              %s
              %s

              %s
              %s
            """.formatted(
                $profileName,
                highlight("--if-exists"),
                renderComment("Example fix:"),
                renderCommand(originalArgs(), "--if-exists"),
                renderComment("See your existing profiles:"),
                renderCommand("astra config list")
            ));

            case ProfileDeleted() -> """
              Profile %s deleted successfully.
            """.formatted(highlight($profileName));
        };

        return OutputAll.message(trimIndent(message));
    }

    @Override
    protected Operation<ConfigDeleteResult> mkOperation() {
        return new ConfigDeleteOperation(config(), new CreateDeleteRequest($profileName, $ifExists));
    }
}
