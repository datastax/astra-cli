package com.dtsx.astra.cli.operations.db.cdc;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.cdc.CdcGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

@RequiredArgsConstructor
public class CdcListOperation {
    private final CdcGateway cdcGateway;

    public record CdcInfo(
        String id,
        String keyspace,
        String table,
        String cluster,
        String namespace,
        String tenant,
        String status
    ) {}

    public List<CdcInfo> execute(DbRef dbRef) {
        val result = cdcGateway.findAll(dbRef);

        return result.stream()
            .map((cdc) -> new CdcInfo(
                cdc.getConnectorName(),
                cdc.getKeyspace(),
                cdc.getDatabaseTable(),
                cdc.getClusterName(),
                cdc.getNamespace(),
                cdc.getTenant(),
                cdc.getCodStatus()
            ))
            .toList();
    }
}
