package com.dtsx.astra.cli.core.exceptions.db;

import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.ExitCode;

public class KeyspaceNotFoundException extends AstraCliException {
    public KeyspaceNotFoundException(KeyspaceRef keyspaceRef) {
        super("Keyspace %s not found. Please check that the keyspace name is correct and that you have access to it.".formatted(keyspaceRef.highlight()));
    }
}
