package com.dtsx.astra.cli.commands.db.cdc;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.cdc.CdcListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;

@Command(
    name = "list-cdcs"
)
public final class CdcListCmd extends AbstractCdcCmd {
    @Override
    protected OutputAll execute() {
        val result = new CdcListOperation(cdcGateway).execute(dbRef);

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
}