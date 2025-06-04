package com.dtsx.astra.cli.exceptions.db;

import com.dtsx.astra.cli.exceptions.AstraCliException;
import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.output.ExitCode;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;

import java.util.List;

public class UnexpectedDatabaseStatusException extends AstraCliException {
    public UnexpectedDatabaseStatusException(String dbName, DatabaseStatusType got, List<DatabaseStatusType> expected) {
        super(AstraColors.RED_500.use("""
            """
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
