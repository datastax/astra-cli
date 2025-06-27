package com.dtsx.astra.cli.core.exceptions;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.ParameterException;

@RequiredArgsConstructor
public class ParameterExceptionHandler implements IParameterExceptionHandler {
    private final IParameterExceptionHandler defaultHandler;

    @Override
    public int handleParseException(ParameterException ex, String[] args) throws Exception {
        if (ex.getCause() instanceof AstraCliException cliErr) {
            return ExceptionHandlerUtils.handleAstraCliException(cliErr, ex.getCommandLine());
        }

        return defaultHandler.handleParseException(ex, args);
    }
}
