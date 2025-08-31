package com.dtsx.astra.cli.testlib.doubles.gateways;

import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;

import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.testlib.doubles.Utils.methodIllegallyCalled;

public class TableGatewayStub implements TableGateway {
    @Override
    public Optional<TableDefinition> findOne(TableRef collRef) {
        return methodIllegallyCalled();
    }

    @Override
    public List<TableDescriptor> findAll(KeyspaceRef ksRef) {
        return methodIllegallyCalled();
    }

    @Override
    public DeletionStatus<TableRef> delete(TableRef collRef) {
        return methodIllegallyCalled();
    }

    @Override
    public DeletionStatus<TableRef> truncate(TableRef collRef) {
        return methodIllegallyCalled();
    }
}
