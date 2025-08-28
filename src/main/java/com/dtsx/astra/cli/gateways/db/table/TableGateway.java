package com.dtsx.astra.cli.gateways.db.table;

import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;
import java.util.Optional;

public interface TableGateway {
    static TableGateway mkDefault(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return new TableGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx));
    }

    Optional<TableDefinition> findOne(TableRef collRef);

    List<TableDescriptor> findAll(KeyspaceRef ksRef);

    DeletionStatus<TableRef> delete(TableRef collRef);

    DeletionStatus<TableRef> truncate(TableRef collRef);
}
