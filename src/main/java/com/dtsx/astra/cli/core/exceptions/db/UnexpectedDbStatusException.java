package com.dtsx.astra.cli.core.exceptions.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.ExitCode;
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
}
