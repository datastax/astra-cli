package com.dtsx.astra.cli.core.exceptions;

import com.dtsx.astra.cli.core.exceptions.external.AuthenticationExceptionMapper;
import com.dtsx.astra.cli.core.exceptions.external.DatabaseNotFoundExceptionMapper;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.ExitCode;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExecutionExceptionHandler implements IExecutionExceptionHandler {
    private static final Map<Class<? extends Exception>, ExternalExceptionMapper<?>> EXTERNAL_ERROR_MAPPERS =
        Stream.of(
            new DatabaseNotFoundExceptionMapper(),
            new AuthenticationExceptionMapper()
        ).collect(
            Collectors.toMap(ExternalExceptionMapper::getExceptionClass, Function.identity())
        );

    @Override
    public int handleExecutionException(Exception unmappedE, CommandLine cmd, ParseResult parseResult) {
        val e = (EXTERNAL_ERROR_MAPPERS.containsKey(unmappedE.getClass()))
            ? EXTERNAL_ERROR_MAPPERS.get(unmappedE.getClass()).mapException(unmappedE, cmd, parseResult)
            : unmappedE;

        if (e instanceof AstraCliException cliErr) {
            return ExceptionHandlerUtils.handleAstraCliException(cliErr, cmd);
        }

        e.printStackTrace(AstraConsole.getErr());
        return ExitCode.INTERNAL_ERROR.getCode();
    }

    public interface ExternalExceptionMapper<E extends Exception> {
        Class<E> getExceptionClass();
        AstraCliException mapExceptionInternal(E ex, CommandLine commandLine, ParseResult fullParseResult);

        @SuppressWarnings("unchecked")
        default AstraCliException mapException(Exception ex, CommandLine commandLine, ParseResult fullParseResult) {
            return mapExceptionInternal((E) ex, commandLine, fullParseResult);
        }
    }
}
