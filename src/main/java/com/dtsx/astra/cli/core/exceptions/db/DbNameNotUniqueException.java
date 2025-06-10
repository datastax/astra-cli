package com.dtsx.astra.cli.core.exceptions.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.ExitCode;

public class DbNameNotUniqueException extends AstraCliException {
    public DbNameNotUniqueException(String dbName) {
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
