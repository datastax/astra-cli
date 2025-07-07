package com.dtsx.astra.cli.core.exceptions.internal.config;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;

import java.io.File;

public class AstraConfigFileException extends AstraCliException {
    public AstraConfigFileException(String message, File file) {
        super("@|bold,red An error occurred parsing the configuration file '%s':|@%n%n%s".formatted(file.getAbsolutePath(), message));
    }
}
