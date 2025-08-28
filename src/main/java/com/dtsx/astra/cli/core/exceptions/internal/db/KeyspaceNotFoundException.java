package com.dtsx.astra.cli.core.exceptions.internal.db;

import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;

public class KeyspaceNotFoundException extends AstraCliException {
    public KeyspaceNotFoundException(KeyspaceRef keyspaceRef) {
        super("Keyspace @!%s!@ not found. Please check that the keyspace name is correct and that you have access to it.".formatted(keyspaceRef));
    }
}
