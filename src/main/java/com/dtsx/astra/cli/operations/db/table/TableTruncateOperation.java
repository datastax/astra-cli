package com.dtsx.astra.cli.operations.db.table;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableTruncateOperation.TableTruncateResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class TableTruncateOperation implements Operation<TableTruncateResult> {
    private final TableGateway tableGateway;
    private final TableTruncateRequest request;

    public sealed interface TableTruncateResult {}
    public record TableTruncated() implements TableTruncateResult {}
    public record TableNotFound() implements TableTruncateResult {}

    public record TableTruncateRequest(TableRef tableRef) {}

    @Override
    public TableTruncateResult execute() {
        val status = tableGateway.truncate(request.tableRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> new TableTruncated();
            case DeletionStatus.NotFound<?> _ -> new TableNotFound();
        };
    }

}