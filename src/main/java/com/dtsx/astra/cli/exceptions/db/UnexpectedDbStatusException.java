package com.dtsx.astra.cli.exceptions.db;

import com.dtsx.astra.cli.domain.db.DbRef;
import com.dtsx.astra.cli.exceptions.AstraCliException;
import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.output.ExitCode;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;

import java.util.List;

public class UnexpectedDbStatusException extends AstraCliException {
    public UnexpectedDbStatusException(DbRef ref, DatabaseStatusType got, List<DatabaseStatusType> expected) {
        super(AstraColors.RED_500.use("""
            @|bold Database %s has unexpected status '%s'.|@
            
            Expected one of: %s
            
            The database may be in a transitional state. Please wait a few moments and try again, or check the database status in the Astra console.
            """
            .stripIndent().formatted(
                ref.toString(),
                got.toString(),
                expected.stream().map(DatabaseStatusType::toString).toList()
            )));
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
