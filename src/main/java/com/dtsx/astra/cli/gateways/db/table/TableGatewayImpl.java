package com.dtsx.astra.cli.gateways.db.table;

import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.datastax.astra.client.exceptions.DataAPIException;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.APIProviderImpl;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class TableGatewayImpl implements TableGateway {
    private final APIProviderImpl api;

    public TableGatewayImpl(AstraToken token, AstraEnvironment env) {
        this.api = (APIProviderImpl) APIProvider.mkDefault(token, env);
    }

    @Override
    public List<TableDescriptor> findAll(KeyspaceRef ksRef) {
        return AstraLogger.loading("Listing tables for keyspace " + highlight(ksRef), (_) ->
            api.dataApiDatabase(ksRef).listTables()
        );
    }

    @Override
    public Optional<TableDefinition> findOne(TableRef collRef) {
        try {
            return AstraLogger.loading("Getting table " + highlight(collRef), (_) -> {
                return Optional.of(
                    api.dataApiDatabase(collRef.keyspace()).getTable(collRef.name()).getDefinition()
                );
            });
        } catch (DataAPIException e) {
            if (e.getErrorCode().equals("TABLE_NOT_EXIST")) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public DeletionStatus<TableRef> delete(TableRef collRef) {
        if (!exists(collRef)) {
            return DeletionStatus.notFound(collRef);
        }

        AstraLogger.loading("Deleting table " + highlight(collRef), (_) -> {
            api.dataApiDatabase(collRef.keyspace()).dropTable(collRef.name());
            return null;
        });

        return DeletionStatus.deleted(collRef);
    }

    @Override
    public DeletionStatus<TableRef> truncate(TableRef collRef) {
        if (!exists(collRef)) {
            return DeletionStatus.notFound(collRef);
        }

        AstraLogger.loading("Truncating table " + highlight(collRef), (_) -> {
            api.dataApiDatabase(collRef.keyspace()).getTable(collRef.name()).deleteAll();
            return null;
        });

        return DeletionStatus.deleted(collRef);
    }

    private boolean exists(TableRef collRef) {
        return AstraLogger.loading("Checking if table " + highlight(collRef) + " exists", (_) -> {
            return findOne(collRef).isPresent();
        });
    }
}
