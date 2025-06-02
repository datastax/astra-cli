package com.dtsx.astra.cli.exceptions.db;

import com.dtsx.astra.cli.exceptions.AstraCliException;
import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.output.ExitCode;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class CannotCreateFileException extends AstraCliException {
    public CannotCreateFileException(File file, @Nullable String message, Throwable cause) {
        super(
            AstraColors.RED_500.use("@|bold An error occurred while trying to create file '%s':%n%n%s|@").formatted(file.getAbsolutePath(), cause.getMessage()) + (message == null ? "" : "%n%n%s".formatted(message)),
            cause
        );
    }

    @Override
    public boolean shouldDumpLogs() {
        return false;
    }

    @Override
    public ExitCode getExitCode() {
        return ExitCode.CANNOT_CONNECT;
    }
}
