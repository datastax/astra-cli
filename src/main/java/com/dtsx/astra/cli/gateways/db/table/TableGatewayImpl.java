package com.dtsx.astra.cli.gateways.db.table;

import com.datastax.astra.client.exceptions.DataAPIException;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TableGatewayImpl implements TableGateway {
    private final CliContext ctx;
    private final APIProvider api;

    @Override
    public List<TableDescriptor> findAll(KeyspaceRef ksRef) {
        return ctx.log().loading("Listing tables for keyspace " + ctx.highlight(ksRef), (_) ->
            api.dataApiDatabase(ksRef).listTables()
        );
    }

    @Override
    public Optional<TableDefinition> findOne(TableRef collRef) {
        try {
            return ctx.log().loading("Getting table " + ctx.highlight(collRef), (_) -> {
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

        ctx.log().loading("Deleting table " + ctx.highlight(collRef), (_) -> {
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

        ctx.log().loading("Truncating table " + ctx.highlight(collRef), (_) -> {
            api.dataApiDatabase(collRef.keyspace()).getTable(collRef.name()).deleteAll();
            return null;
        });

        return DeletionStatus.deleted(collRef);
    }

    private boolean exists(TableRef collRef) {
        return ctx.log().loading("Checking if table " + ctx.highlight(collRef) + " exists", (_) -> {
            return findOne(collRef).isPresent();
        });
    }
}
