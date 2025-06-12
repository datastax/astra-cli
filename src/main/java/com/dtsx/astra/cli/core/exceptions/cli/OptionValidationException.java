package com.dtsx.astra.cli.core.exceptions.cli;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.ExitCode;

public class OptionValidationException extends AstraCliException {
    public OptionValidationException(String parameter, String message) {
        super("@|bold,red Error validating %s: \"%s\"|@".formatted(parameter, message));
    }
}
