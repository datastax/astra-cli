package com.dtsx.astra.cli.core.exceptions.external;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.ExecutionExceptionHandler.ExternalExceptionMapper;
import com.dtsx.astra.cli.core.exceptions.internal.cli.ExecutionCancelledException;
import picocli.CommandLine;

public class UserInterruptExceptionMapper implements ExternalExceptionMapper<Exception> {
    @Override
    public boolean canMap(Exception ex) {
        return ex.getClass().getName().equals("jdk.internal.org.jline.reader.UserInterruptException");
    }

    @Override
    public AstraCliException mapExceptionInternal(Exception ex, CommandLine commandLine, CommandLine.ParseResult fullParseResult) {
        return new ExecutionCancelledException();
    }
}
