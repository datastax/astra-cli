package com.dtsx.astra.cli.core.exceptions.internal.cli;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;

public class OptionValidationException extends AstraCliException {
    public OptionValidationException(String parameter, String message) {
        super("@|bold,red Error validating %s: \"%s\"|@".formatted(parameter, message));
    }

    @Override
    public boolean shouldPrintHelpMessage() {
        return true;
    }
}
