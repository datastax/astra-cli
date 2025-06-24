package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.operations.db.table.TableDeleteOperation;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.table.TableDeleteOperation.*;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "delete-table"
)
public final class TableDeleteCmd extends AbstractTableSpecificCmd {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if table does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifExists;

    @Override
    public OutputAll execute() {
        val result = new TableDeleteOperation(tableGateway).execute(tableRef, ifExists);

        return switch (result) {
            case TableNotFound() -> {
                yield OutputAll.message("Table " + highlight(tableRef) + " does not exist; nothing to delete");
            }
            case TableDeleted() -> {
                yield OutputAll.message("Table %s has been deleted from keyspace %s".formatted(highlight(tableRef.name()), highlight(keyspaceRef)));
            }
        };
    }
}
