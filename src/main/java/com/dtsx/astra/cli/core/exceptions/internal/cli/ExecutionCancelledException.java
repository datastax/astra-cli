package com.dtsx.astra.cli.core.exceptions.internal.cli;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;

import static com.dtsx.astra.cli.core.output.ExitCode.EXECUTION_CANCELLED;

public class ExecutionCancelledException extends AstraCliException {
    public ExecutionCancelledException() {
        this("@|bold,red Operation cancelled by user|@");
    }

    public ExecutionCancelledException(String message) {
        super(EXECUTION_CANCELLED, message);
    }
}
