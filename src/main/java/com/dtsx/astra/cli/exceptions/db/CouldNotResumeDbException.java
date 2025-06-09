package com.dtsx.astra.cli.exceptions.db;

import com.dtsx.astra.cli.domain.db.DbRef;
import com.dtsx.astra.cli.exceptions.AstraCliException;
import com.dtsx.astra.cli.output.ExitCode;

import static com.dtsx.astra.cli.output.AstraColors.highlight;

public class CouldNotResumeDbException extends AstraCliException {
    public CouldNotResumeDbException(DbRef dbRef, String error) {
        super("Could not resume database %s: %s".formatted(highlight(dbRef), error));
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
