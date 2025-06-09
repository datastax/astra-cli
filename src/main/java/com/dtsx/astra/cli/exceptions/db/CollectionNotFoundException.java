package com.dtsx.astra.cli.exceptions.db;

import com.dtsx.astra.cli.domain.db.collections.CollectionRef;
import com.dtsx.astra.cli.exceptions.AstraCliException;
import com.dtsx.astra.cli.output.ExitCode;

public class CollectionNotFoundException extends AstraCliException {
    public CollectionNotFoundException(CollectionRef collectionRef) {
        super("Collection %s not found. Please check that the collection name is correct and that you have access to it.".formatted(collectionRef.highlight()));
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