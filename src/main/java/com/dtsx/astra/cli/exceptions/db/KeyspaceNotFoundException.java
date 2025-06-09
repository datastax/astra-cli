package com.dtsx.astra.cli.exceptions.db;

import com.dtsx.astra.cli.domain.db.keyspaces.KeyspaceRef;
import com.dtsx.astra.cli.exceptions.AstraCliException;
import com.dtsx.astra.cli.output.ExitCode;

public class KeyspaceNotFoundException extends AstraCliException {
    public KeyspaceNotFoundException(KeyspaceRef keyspaceRef) {
        super("Keyspace %s not found. Please check that the keyspace name is correct and that you have access to it.".formatted(keyspaceRef.highlight()));
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