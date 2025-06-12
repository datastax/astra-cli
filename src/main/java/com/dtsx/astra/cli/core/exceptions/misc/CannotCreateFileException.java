package com.dtsx.astra.cli.core.exceptions.misc;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.ExitCode;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class CannotCreateFileException extends AstraCliException {
    public CannotCreateFileException(File file, @Nullable String message, Throwable cause) {
        super(
            AstraColors.RED_500.use("@|bold An error occurred while trying to create file '%s':%n%n%s|@").formatted(file.getAbsolutePath(), cause.getMessage()) + (message == null ? "" : "%n%n%s".formatted(message)),
            cause
        );
    }
}
