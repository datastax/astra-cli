package com.dtsx.astra.cli.core.exceptions.internal.misc;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class CannotCreateFileException extends AstraCliException {
    public CannotCreateFileException(Path file, @Nullable String message, Throwable cause) {
        super(
            AstraColors.RED_500.use("@|bold An error occurred while trying to create file '%s':%n%n%s|@").formatted(file.toString(), cause.getMessage()) + (message == null ? "" : "%n%n%s".formatted(message))
        );
    }
}
