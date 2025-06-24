package com.dtsx.astra.cli.operations.db.table;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class TableTruncateOperation {
    private final TableGateway tableGateway;

    public void execute(TableRef tableRef) {
        val status = tableGateway.truncateTable(tableRef);

        if (status instanceof DeletionStatus.NotFound<?>) {
            throw new TableNotFoundException(tableRef);
        }
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