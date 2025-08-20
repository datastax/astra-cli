package com.dtsx.astra.cli.core.exceptions.internal.misc;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;

public class InvalidTokenException extends AstraCliException {
    public InvalidTokenException(String reason) {
        super("@|bold,red Invalid Astra token: %s|@".formatted(reason));
    }
}
