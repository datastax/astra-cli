package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.table.TableTruncateOperation;
import picocli.CommandLine.Command;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "truncate-table"
)
public final class TableTruncateCmd extends AbstractTableSpecificCmd {
    @Override
    public OutputAll execute() {
        new TableTruncateOperation(tableGateway).execute(tableRef);

        return OutputAll.message(
            "Table %s has been truncated in keyspace %s".formatted(highlight(tableRef.name()), highlight(keyspaceRef))
        );
    }
}
