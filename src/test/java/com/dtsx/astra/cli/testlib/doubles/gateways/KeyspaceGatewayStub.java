package com.dtsx.astra.cli.testlib.doubles.gateways;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;

import static com.dtsx.astra.cli.testlib.doubles.Utils.methodIllegallyCalled;

public class KeyspaceGatewayStub implements KeyspaceGateway {
    @Override
    public FoundKeyspaces findAll(DbRef dbRef) {
        return methodIllegallyCalled();
    }

    @Override
    public CreationStatus<KeyspaceRef> create(KeyspaceRef keyspaceRef) {
        return methodIllegallyCalled();
    }

    @Override
    public DeletionStatus<KeyspaceRef> delete(KeyspaceRef keyspaceRef) {
        return methodIllegallyCalled();
    }
}
