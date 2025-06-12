package com.dtsx.astra.cli.core.exceptions.external;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.ExecutionExceptionHandler;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import lombok.val;
import picocli.CommandLine;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

public class DatabaseNotFoundExceptionMapper implements ExecutionExceptionHandler.ExternalExceptionMapper<DatabaseNotFoundException> {
    @Override
    public Class<DatabaseNotFoundException> getExceptionClass() {
        return DatabaseNotFoundException.class;
    }

    @Override
    public AstraCliException mapExceptionInternal(DatabaseNotFoundException ex, CommandLine commandLine, CommandLine.ParseResult fullParseResult) {
        val message = ex.getMessage();

        val dbName = (message != null && message.startsWith("Database '") && message.endsWith("' has not been found."))
            ? message.substring(10, message.length() - 21)
            : null;

        val msg = """
            %s
            
            Please ensure that:
              - You are using the correct organization.
              - You are using the correct database name.
            
            You may use %s to list all databases in the current org.
            
            You may use the %s and %s flags to use a different organizational token."""
            .stripIndent().formatted(
                AstraColors.RED_500.use("@|bold ERROR: The %s could not be found|@").formatted((dbName != null) ? "database '" + dbName + "'" : "given database"),
                highlight("astra db list"),
                highlight("--profile"),
                highlight("--token")
            );

        return new AstraCliException(msg) {};
    }
}
