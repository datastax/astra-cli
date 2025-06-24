package com.dtsx.astra.cli.gateways.db.cdc;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CdcRef;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.utils.MiscUtils;
import com.dtsx.astra.sdk.streaming.domain.CdcDefinition;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.utils.MiscUtils.*;

@RequiredArgsConstructor
public class CdcGatewayImpl implements CdcGateway {
    private final APIProvider api;

    @Override
    public List<CdcDefinition> findAll(DbRef dbRef) {
        return AstraLogger.loading("Finding all CDC definitions for db " + highlight(dbRef), (_) -> (
            api.dbOpsClient(dbRef)
                .cdc()
                .findAll()
                .toList()
        ));
    }

    @Override
    public CreationStatus<Void> create(TableRef tableRef, TenantName tenantName, int topicPartition) {
        val cdcRef = CdcRef.fromDefinition(tableRef, tenantName);
        
        if (exists(cdcRef)) {
            return CreationStatus.alreadyExists(null);
        }

        return AstraLogger.loading("Creating CDC " + highlight(cdcRef), (_) -> {
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

        return AstraLogger.loading("Deleting CDC " + highlight(cdcRef), (_) -> {
            val client = api.dbOpsClient(cdcRef.db()).cdc();

            cdcRef.fold(
                toFn((id) -> client.delete(id.unwrap())),
                toFn((table, tenant) -> client.delete(table.keyspace().name(), table.name(), tenant.unwrap()))
            );

            return DeletionStatus.deleted(null);
        });
    }

    private boolean exists(CdcRef cdcRef) {
        return AstraLogger.loading("Checking if cdc " + highlight(cdcRef) + " exists", (_) -> {
            val client = api.dbOpsClient(cdcRef.db()).cdc();

            return cdcRef.fold(
                (id) -> client.findById(id.unwrap()),
                (table, tenant) -> client.findByDefinition(table.keyspace().name(), table.name(), tenant.unwrap())
            ).isPresent();
        });
    }
}
