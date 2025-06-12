package com.dtsx.astra.cli.core.exceptions.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.ExitCode;

import java.util.UUID;

public class DbAlreadyExistsException extends AstraCliException {
    public DbAlreadyExistsException(DbRef dbRef, UUID dbId) {
        super("Database %s already exists with id %s".formatted(dbRef, dbId));
    }
}
