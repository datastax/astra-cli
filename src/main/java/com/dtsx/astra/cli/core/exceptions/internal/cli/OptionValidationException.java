package com.dtsx.astra.cli.core.exceptions.internal.cli;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;

import static com.dtsx.astra.cli.core.output.ExitCode.VALIDATION_ISSUE;

public class OptionValidationException extends AstraCliException {
    public OptionValidationException(String parameter, String message) {
        super(VALIDATION_ISSUE, "@|bold,red Error validating %s: \"%s\"|@".formatted(parameter, message));
    }

    @Override
    public boolean shouldPrintHelpMessage() {
        return true;
    }
}
