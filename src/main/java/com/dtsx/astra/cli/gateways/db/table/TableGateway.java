package com.dtsx.astra.cli.gateways.db.table;

import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;
import java.util.Optional;

public interface TableGateway {
    static TableGateway mkDefault(AstraToken token, AstraEnvironment env) {
        return new TableGatewayImpl(token, env);
    }

    Optional<TableDefinition> findOne(TableRef collRef);

    List<TableDescriptor> findAll(KeyspaceRef ksRef);

    DeletionStatus<TableRef> delete(TableRef collRef);

    DeletionStatus<TableRef> truncate(TableRef collRef);
}
