package com.dtsx.astra.cli.gateways.db.cdc;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CdcRef;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.streaming.domain.CdcDefinition;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.stream.Stream;

import static com.dtsx.astra.cli.utils.MiscUtils.toFn;

@RequiredArgsConstructor
public class CdcGatewayImpl implements CdcGateway {
    private final CliContext ctx;
    private final APIProvider api;

    @Override
    public Stream<CdcDefinition> findAll(DbRef dbRef) {
        return ctx.log().loading("Finding all CDC definitions for db " + ctx.highlight(dbRef), (_) -> (
            api.dbOpsClient(dbRef)
                .cdc()
                .findAll()
        ));
    }

    @Override
    public CreationStatus<Void> create(TableRef tableRef, TenantName tenantName, int topicPartition) {
        val cdcRef = CdcRef.fromDefinition(tableRef, tenantName);
        
        if (exists(cdcRef)) {
            return CreationStatus.alreadyExists(null);
        }

        return ctx.log().loading("Creating CDC " + ctx.highlight(cdcRef), (_) -> {
            api.dbOpsClient(tableRef.db())
                .cdc()
                .create(tableRef.keyspace().name(), tableRef.name(), tenantName.unwrap(), topicPartition);

            return CreationStatus.created(null);
        });
    }

    @Override
    public DeletionStatus<Void> delete(CdcRef cdcRef) {
        if (!exists(cdcRef)) {
            return DeletionStatus.notFound(null);
        }

        return ctx.log().loading("Deleting CDC " + ctx.highlight(cdcRef), (_) -> {
            val client = api.dbOpsClient(cdcRef.db()).cdc();

            cdcRef.fold(
                toFn((id) -> client.delete(id.unwrap())),
                toFn((table, tenant) -> client.delete(table.keyspace().name(), table.name(), tenant.unwrap()))
            );

            return DeletionStatus.deleted(null);
        });
    }

    private boolean exists(CdcRef cdcRef) {
        return ctx.log().loading("Checking if cdc " + ctx.highlight(cdcRef) + " exists", (_) -> {
            val client = api.dbOpsClient(cdcRef.db()).cdc();

            return cdcRef.fold(
                (id) -> client.findById(id.unwrap()),
                (table, tenant) -> client.findByDefinition(table.keyspace().name(), table.name(), tenant.unwrap())
            ).isPresent();
        });
    }
}
