package com.dtsx.astra.cli.core.exceptions.internal.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

public class CouldNotResumeDbException extends AstraCliException {
    public CouldNotResumeDbException(DbRef dbRef, String error) {
        super("Could not resume database %s: %s".formatted(highlight(dbRef), error));
    }
}
