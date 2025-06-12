package com.dtsx.astra.cli.core.exceptions.config;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.ExitCode;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class AstraConfigFileException extends AstraCliException {
    public AstraConfigFileException(String message, File file) {
        super("@|bold,red An error occurred parsing the configuration file '%s':|@%n%n%s".formatted(file.getAbsolutePath(), message));
    }
}
