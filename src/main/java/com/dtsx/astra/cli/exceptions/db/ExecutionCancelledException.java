package com.dtsx.astra.cli.exceptions.db;

import com.dtsx.astra.cli.exceptions.AstraCliException;
import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.output.ExitCode;

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
