package com.dtsx.astra.cli.core.exceptions.internal.misc;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;

public class InvalidTokenException extends AstraCliException {
    public InvalidTokenException(String reason) {
        super(
            AstraColors.RED_500.use("@|bold Invalid Astra token:|@ %s".formatted(reason))
        );
    }
}
