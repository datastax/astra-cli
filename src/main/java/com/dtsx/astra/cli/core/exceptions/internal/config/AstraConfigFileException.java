package com.dtsx.astra.cli.core.exceptions.internal.config;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;

import java.nio.file.Path;

public class AstraConfigFileException extends AstraCliException {
    public AstraConfigFileException(String message, Path file) {
        super("@|bold,red An error occurred parsing the configuration file '%s':|@%n%n%s".formatted(file.toString(), message));
    }
}
