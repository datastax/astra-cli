package com.dtsx.astra.cli.operations.db.cdc;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.cdc.CdcGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cdc.CdcListOperation.CdcInfo;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

@RequiredArgsConstructor
public class CdcListOperation implements Operation<List<CdcInfo>> {
    private final CdcGateway cdcGateway;
    private final CdcListRequest request;

    public record CdcInfo(
        String id,
        String keyspace,
        String table,
        String cluster,
        String namespace,
        String tenant,
        String status
    ) {}

    public record CdcListRequest(DbRef dbRef) {}

    @Override
    public List<CdcInfo> execute() {
        val dbRef = request.dbRef;
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
