package com.dtsx.astra.cli.gateways.db.keyspace;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.SomeGateway;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface KeyspaceGateway extends SomeGateway {
    record FoundKeyspaces(
        @Nullable String defaultKeyspace,
        List<String> keyspaces
    ) {}

    FoundKeyspaces findAll(DbRef dbRef);

    CreationStatus<KeyspaceRef> create(KeyspaceRef keyspaceRef);

    DeletionStatus<KeyspaceRef> delete(KeyspaceRef keyspaceRef);
}
