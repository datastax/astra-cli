package com.dtsx.astra.cli.core.exceptions;

import com.dtsx.astra.cli.core.CliContext;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.ParameterException;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class ParameterExceptionHandler implements IParameterExceptionHandler {
    private final IParameterExceptionHandler defaultHandler;
    private final Supplier<CliContext> ctx;

    @Override
    public int handleParseException(ParameterException ex, String[] args) throws Exception {
        if (ex.getCause() instanceof AstraCliException cliErr) {
            return ExceptionHandlerUtils.handleAstraCliException(cliErr, ex.getCommandLine(), ctx.get());
        }

        return defaultHandler.handleParseException(ex, args);
    }
}
