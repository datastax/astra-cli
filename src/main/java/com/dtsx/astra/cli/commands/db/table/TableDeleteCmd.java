package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.table.TableDeleteOperation.*;

@Command(
    name = "delete-table"
)
public class TableDeleteCmd extends AbstractTableSpecificCmd<TableDeleteResult> {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if table does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifExists;

    @Override
    public final OutputAll execute(TableDeleteResult result) {
        val message = switch (result) {
            case TableNotFound() -> "Table " + highlight(tableRef) + " does not exist; nothing to delete";
            case TableIllegallyNotFound() -> throw new TableNotFoundException(tableRef);
            case TableDeleted() -> "Table %s has been deleted from keyspace %s".formatted(highlight(tableRef.name()), highlight(keyspaceRef));
        };
        
        return OutputAll.message(message);
    }

    @Override
    protected Operation<TableDeleteResult> mkOperation() {
        return new TableDeleteOperation(tableGateway, new TableDeleteRequest(tableRef, ifExists));
    }

    public static class TableNotFoundException extends AstraCliException {
        public TableNotFoundException(TableRef tableRef) {
            super("""
              @|bold,red Error: Table '%s' does not exist in database '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see all existing tables in this database.
              - Pass the %s flag to skip this error if the table doesn't exist.
            """.formatted(
                tableRef,
                tableRef.db(),
                AstraColors.highlight("astra db list-tables " + tableRef.db() + " --all"),
                AstraColors.highlight("--if-exists")
            ));
        }
    }
}
