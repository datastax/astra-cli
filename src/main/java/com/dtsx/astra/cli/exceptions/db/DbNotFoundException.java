package com.dtsx.astra.cli.exceptions.db;

import com.dtsx.astra.cli.domain.db.DbRef;
import com.dtsx.astra.cli.exceptions.AstraCliException;
import com.dtsx.astra.cli.output.ExitCode;

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
