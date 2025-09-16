package com.dtsx.astra.cli.core.exceptions;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Ref;
import com.dtsx.astra.cli.core.exceptions.external.AuthenticationExceptionMapper;
import com.dtsx.astra.cli.core.exceptions.external.DatabaseNotFoundExceptionMapper;
import com.dtsx.astra.cli.core.exceptions.external.UserInterruptExceptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

import java.util.List;

@RequiredArgsConstructor
public class ExecutionExceptionHandler implements IExecutionExceptionHandler {
    private static final List<ExternalExceptionMapper<?>> EXTERNAL_ERROR_MAPPERS =
        List.of(
            new DatabaseNotFoundExceptionMapper(),
            new AuthenticationExceptionMapper(),
            new UserInterruptExceptionMapper()
        );

    private final Ref<CliContext> ctxRef;

    @Override
    public int handleExecutionException(Exception unmappedE, CommandLine cmd, ParseResult parseResult) {
        val e = EXTERNAL_ERROR_MAPPERS.stream()
            .filter(m -> m.canMap(unmappedE))
            .findFirst()
            .<Exception>map(m -> m.mapException(unmappedE, cmd, parseResult, ctxRef.get()))
            .orElse(unmappedE);

        if (e instanceof AstraCliException err) {
            return ExceptionHandlerUtils.handleAstraCliException(err, cmd, ctxRef.get());
        }

        if (e instanceof ExitCodeException err) {
            return err.exitCode();
        }

        return ExceptionHandlerUtils.handleUncaughtException(unmappedE, ctxRef.get());
    }

    public interface ExternalExceptionMapper<E extends Exception> {
        boolean canMap(Exception ex);
        AstraCliException mapExceptionInternal(E ex, CommandLine commandLine, ParseResult fullParseResult, CliContext ctx);

        @SuppressWarnings("unchecked")
        default AstraCliException mapException(Exception ex, CommandLine commandLine, ParseResult fullParseResult, CliContext ctx) {
            return mapExceptionInternal((E) ex, commandLine, fullParseResult, ctx);
        }
    }
}
