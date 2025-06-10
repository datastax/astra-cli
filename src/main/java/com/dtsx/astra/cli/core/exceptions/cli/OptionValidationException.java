package com.dtsx.astra.cli.core.exceptions.cli;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.ExitCode;

public class OptionValidationException extends AstraCliException {
    public OptionValidationException(String parameter, String message) {
        super(AstraColors.RED_500.use("@|bold Error validating %s: \"%s\"|@".formatted(parameter, message)));
    }

    @Override
    public boolean shouldDumpLogs() {
        return false;
    }

    @Override
    public ExitCode getExitCode() {
        return ExitCode.INVALID_PARAMETER;
    }
}
