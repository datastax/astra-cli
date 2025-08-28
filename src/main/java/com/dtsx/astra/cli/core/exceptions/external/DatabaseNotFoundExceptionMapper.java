package com.dtsx.astra.cli.core.exceptions.external;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.ExecutionExceptionHandler.ExternalExceptionMapper;
import com.dtsx.astra.cli.core.exceptions.internal.db.DbNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import picocli.CommandLine;

public class DatabaseNotFoundExceptionMapper implements ExternalExceptionMapper<DatabaseNotFoundException> {
    @Override
    public boolean canMap(Exception ex) {
        return ex instanceof DatabaseNotFoundException;
    }

    @Override
    public AstraCliException mapExceptionInternal(DatabaseNotFoundException ex, CommandLine commandLine, CommandLine.ParseResult fullParseResult, CliContext ctx) {
        return new DbNotFoundException(DbRef.fromNameUnsafe(ex.getMessage().substring(10, ex.getMessage().length() - 21) + "'"));
    }
}
