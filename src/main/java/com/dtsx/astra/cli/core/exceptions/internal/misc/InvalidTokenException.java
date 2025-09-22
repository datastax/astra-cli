package com.dtsx.astra.cli.core.exceptions.internal.misc;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;

import static com.dtsx.astra.cli.core.output.ExitCode.INVALID_TOKEN;

public class InvalidTokenException extends AstraCliException {
    public InvalidTokenException(String reason) {
        super(INVALID_TOKEN, "@|bold,red Invalid Astra token: %s|@".formatted(reason));
    }
}
