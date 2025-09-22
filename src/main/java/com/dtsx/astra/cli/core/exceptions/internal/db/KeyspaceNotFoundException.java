package com.dtsx.astra.cli.core.exceptions.internal.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.KeyspaceRef;

import static com.dtsx.astra.cli.core.output.ExitCode.KEYSPACE_NOT_FOUND;

public class KeyspaceNotFoundException extends AstraCliException {
    public KeyspaceNotFoundException(KeyspaceRef keyspaceRef) {
        super(KEYSPACE_NOT_FOUND, "Keyspace @!%s!@ not found. Please check that the keyspace name is correct and that you have access to it.".formatted(keyspaceRef));
    }
}
