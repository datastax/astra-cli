package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableTruncateOperation;
import lombok.val;
import picocli.CommandLine.Command;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.table.TableTruncateOperation.*;

@Command(
    name = "truncate-table"
)
public class TableTruncateCmd extends AbstractTableSpecificCmd<TableTruncateResult> {
    @Override
    public final OutputAll execute(TableTruncateResult result) {
        val message = switch (result) {
            case TableTruncated() -> "Table %s has been truncated in keyspace %s".formatted(highlight(tableRef.name()), highlight(keyspaceRef));
            case TableNotFound() -> throw new TableNotFoundException(tableRef);
        };
        
        return OutputAll.message(message);
    }

    @Override
    protected Operation<TableTruncateResult> mkOperation() {
        return new TableTruncateOperation(tableGateway, new TableTruncateRequest(tableRef));
    }

    public static class TableNotFoundException extends AstraCliException {
        public TableNotFoundException(TableRef tableRef) {
            super("""
              @|bold,red Error: Table '%s' does not exist in database '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see all existing tables in this database.
            """.formatted(
                tableRef,
                tableRef.db(),
                AstraColors.highlight("astra db list-tables " + tableRef.db() + " --all")
            ));
        }
    }
}
