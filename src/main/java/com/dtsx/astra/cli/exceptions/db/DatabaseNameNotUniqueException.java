package com.dtsx.astra.cli.exceptions.db;

import com.dtsx.astra.cli.exceptions.AstraCliException;
import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.output.ExitCode;

public class DatabaseNameNotUniqueException extends AstraCliException {
    public DatabaseNameNotUniqueException(String dbName) {
        super(AstraColors.RED_500.use("""
            @|bold Multiple databases with same name '%s' detected.|@
            Please fallback to database id to resolve the conflict."""
            .stripIndent().formatted(dbName)));
    }

    @Override
    public boolean shouldDumpLogs() {
        return false;
    }

    @Override
    public ExitCode getExitCode() {
        return ExitCode.CONFLICT;
    }
}
