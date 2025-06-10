package com.dtsx.astra.cli.core.exceptions.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.ExitCode;

public class DbNotFoundException extends AstraCliException {
    public DbNotFoundException(DbRef dbRef) {
        super("Database %s not found. Please check that the database name or ID is correct and that you have access to it.".formatted(dbRef.toString()));
    }

    @Override
    public boolean shouldDumpLogs() {
        return false;
    }

    @Override
    public ExitCode getExitCode() {
        return ExitCode.CANNOT_CONNECT;
    }
}
