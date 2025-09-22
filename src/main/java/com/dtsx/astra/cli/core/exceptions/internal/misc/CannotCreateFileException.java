package com.dtsx.astra.cli.core.exceptions.internal.misc;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;

public class CannotCreateFileException extends AstraCliException {
    public CannotCreateFileException(Path file, @Nullable String message, Throwable cause) {
        super(FILE_ISSUE, "@|bold,red An error occurred while trying to create file '%s':%n%n%s|@".formatted(file.toString(), cause.getMessage()) + (message == null ? "" : "%n%n%s".formatted(message)));
    }
}
