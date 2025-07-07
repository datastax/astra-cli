package com.dtsx.astra.cli.operations.db.table;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableDeleteOperation.TableDeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class TableDeleteOperation implements Operation<TableDeleteResult> {
    private final TableGateway tableGateway;
    private final TableDeleteRequest request;

    public sealed interface TableDeleteResult {}
    public record TableNotFound() implements TableDeleteResult {}
    public record TableIllegallyNotFound() implements TableDeleteResult {}
    public record TableDeleted() implements TableDeleteResult {}

    public record TableDeleteRequest(TableRef tableRef, boolean ifExists) {}

    @Override
    public TableDeleteResult execute() {
        val status = tableGateway.delete(request.tableRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleTableDeleted();
            case DeletionStatus.NotFound<?> _ -> handleTableNotFound(request.tableRef, request.ifExists);
        };
    }

    private TableDeleteResult handleTableDeleted() {
        return new TableDeleted();
    }

    private TableDeleteResult handleTableNotFound(TableRef tableRef, boolean ifExists) {
        if (ifExists) {
            return new TableNotFound();
        } else {
            return new TableIllegallyNotFound();
        }
    }

}