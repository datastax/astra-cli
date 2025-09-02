package com.dtsx.astra.cli.core.exceptions;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Ref;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.ParameterException;

@RequiredArgsConstructor
public class ParameterExceptionHandler implements IParameterExceptionHandler {
    private final IParameterExceptionHandler defaultHandler;
    private final Ref<CliContext> ctxRef;

    @Override
    public int handleParseException(ParameterException ex, String[] args) throws Exception {
        if (ex.getCause() instanceof AstraCliException cliErr) {
            return ExceptionHandlerUtils.handleAstraCliException(cliErr, ex.getCommandLine(), ctxRef.get());
        }

        return defaultHandler.handleParseException(ex, args);
    }
}
