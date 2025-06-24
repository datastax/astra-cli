package com.dtsx.astra.cli.commands.db.cdc;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cdc.CdcListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.operations.db.cdc.CdcListOperation.*;

@Command(
    name = "list-cdcs"
)
public class CdcListCmd extends AbstractCdcCmd<List<CdcInfo>> {
    @Override
    public final OutputAll execute(List<CdcInfo> result) {
        val data = result.stream()
            .map((cdc) -> Map.of(
                "ID", cdc.id(),
                "Keyspace", cdc.keyspace(),
                "Table", cdc.table(),
                "Cluster", cdc.cluster(),
                "Namespace", cdc.namespace(),
                "Tenant", cdc.tenant(),
                "Status", cdc.status()
            ))
            .toList();

        return new ShellTable(data).withColumns("ID", "Keyspace", "Table", "Cluster", "Namespace", "Tenant", "Status");
    }

    @Override
    protected Operation<List<CdcInfo>> mkOperation() {
        return new CdcListOperation(cdcGateway, new CdcListRequest(dbRef));
    }
}
