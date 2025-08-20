package com.dtsx.astra.cli.commands.db.cdc;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cdc.CdcListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.operations.db.cdc.CdcListOperation.*;

@Command(
    name = "list-cdcs",
    description = "List all CDC (Change Data Capture) connections for the specified database"
)
@Example(
    comment = "List all CDCs in a database",
    command = "astra db list-cdcs my_db"
)
public class CdcListCmd extends AbstractCdcCmd<Stream<CdcInfo>> {
    @Override
    protected OutputJson executeJson(Supplier<Stream<CdcInfo>> result) {
        return OutputJson.serializeValue(result.get().map(CdcInfo::raw).toList());
    }

    @Override
    public final OutputAll execute(Supplier<Stream<CdcInfo>> result) {
        val data = result.get()
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
    protected Operation<Stream<CdcInfo>> mkOperation() {
        return new CdcListOperation(cdcGateway, new CdcListRequest($dbRef));
    }
}
