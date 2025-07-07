package com.dtsx.astra.cli.core.exceptions.internal.cli;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;

public class ExecutionCancelledException extends AstraCliException {
    public ExecutionCancelledException(String message) {
        super("@|bold,red " + message + "|@");
    }
}
