package com.dtsx.astra.cli.core.exceptions.config;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.ExitCode;
import org.jetbrains.annotations.Nullable;

public class AstraConfigFileException extends AstraCliException {
    public AstraConfigFileException(String message, @Nullable Exception cause) {
        super(AstraColors.RED_500.use("@|bold Error regarding the given astrarc configuration file:|@%n%n%s".formatted(message)), cause);
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
