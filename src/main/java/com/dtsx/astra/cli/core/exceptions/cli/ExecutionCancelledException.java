package com.dtsx.astra.cli.core.exceptions.cli;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.ExitCode;

public class ExecutionCancelledException extends AstraCliException {
    public ExecutionCancelledException(String message) {
        super(AstraColors.RED_500.use("@|bold " + message + "|@"));
    }

    @Override
    public boolean shouldDumpLogs() {
        return false;
    }

    @Override
    public ExitCode getExitCode() {
        return ExitCode.CONFIGURATION;
    }
}
