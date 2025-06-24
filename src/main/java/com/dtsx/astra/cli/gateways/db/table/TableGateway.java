package com.dtsx.astra.cli.gateways.db.table;

import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;
import java.util.Optional;

public interface TableGateway {
    static TableGateway mkDefault(String token, AstraEnvironment env) {
        return new TableGatewayImpl(token, env);
    }

    List<TableDescriptor> findAllTables(KeyspaceRef ksRef);

    Optional<TableDefinition> findOneTable(TableRef collRef);

    boolean tableExists(TableRef collRef);

    DeletionStatus<TableRef> deleteTable(TableRef collRef);

    DeletionStatus<TableRef> truncateTable(TableRef collRef);
}
