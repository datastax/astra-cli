package com.dtsx.astra.cli.core.exceptions.config;

import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.CliExceptionCode;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.ExitCode;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.PROFILE_NOT_FOUND;
import static com.dtsx.astra.cli.utils.StringUtils.renderCommand;
import static com.dtsx.astra.cli.utils.StringUtils.renderComment;

public class ProfileNotFoundException extends AstraCliException {
    public ProfileNotFoundException(ProfileName profileName) {
        super(PROFILE_NOT_FOUND, """
          @|bold,red Error: A profile with the name '%s' could not be found.|@

          %s
          %s
        """.formatted(
            profileName.unwrap(),
            renderComment("See your existing profiles:"),
            renderCommand("astra config list")
        ));
    }
}
