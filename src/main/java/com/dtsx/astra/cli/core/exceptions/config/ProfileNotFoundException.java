package com.dtsx.astra.cli.core.exceptions.config;

import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.ExitCode;

public class ProfileNotFoundException extends AstraCliException {
    public ProfileNotFoundException(ProfileName profileName) {
        this(profileName, "");
    }

    public ProfileNotFoundException(ProfileName profileName, String extra) {
        super(
            AstraColors.RED_500.use("@|bold Profile not found:|@ Profile '%s' does not exist%s".formatted(profileName.unwrap(), extra))
        );
    }
}
