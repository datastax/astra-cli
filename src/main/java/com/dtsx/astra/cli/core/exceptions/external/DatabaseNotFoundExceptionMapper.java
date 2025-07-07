package com.dtsx.astra.cli.core.exceptions.external;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.ExecutionExceptionHandler.ExternalExceptionMapper;
import com.dtsx.astra.cli.core.exceptions.internal.db.DbNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import picocli.CommandLine;

public class DatabaseNotFoundExceptionMapper implements ExternalExceptionMapper<DatabaseNotFoundException> {
    @Override
    public Class<DatabaseNotFoundException> getExceptionClass() {
        return DatabaseNotFoundException.class;
    }

    @Override
    public AstraCliException mapExceptionInternal(DatabaseNotFoundException ex, CommandLine commandLine, CommandLine.ParseResult fullParseResult) {
        return new DbNotFoundException(DbRef.fromNameUnsafe(ex.getMessage().substring(10, ex.getMessage().length() - 21) + "'"));
    }
}
