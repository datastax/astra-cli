package com.dtsx.astra.cli.operations.db.table;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class TableDeleteOperation {
    private final TableGateway tableGateway;

    public sealed interface TableDeleteResult {}
    public record TableNotFound() implements TableDeleteResult {}
    public record TableDeleted() implements TableDeleteResult {}

    public TableDeleteResult execute(TableRef tableRef, boolean ifExists) {
        val status = tableGateway.deleteTable(tableRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleTableDeleted();
            case DeletionStatus.NotFound<?> _ -> handleTableNotFound(tableRef, ifExists);
        };
    }

    private TableDeleteResult handleTableDeleted() {
        return new TableDeleted();
    }

    private TableDeleteResult handleTableNotFound(TableRef tableRef, boolean ifExists) {
        if (ifExists) {
            return new TableNotFound();
        } else {
            throw new TableNotFoundException(tableRef);
        }
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