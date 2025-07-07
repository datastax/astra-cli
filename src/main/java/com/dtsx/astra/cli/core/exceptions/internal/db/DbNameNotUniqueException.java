package com.dtsx.astra.cli.core.exceptions.internal.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;

public class DbNameNotUniqueException extends AstraCliException {
    public DbNameNotUniqueException(String dbName) {
        super(AstraColors.RED_500.use("""
            @|bold Multiple databases with same name '%s' detected.|@
            Please fallback to database id to resolve the conflict."""
            .stripIndent().formatted(dbName)));
    }
}
