package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableTruncateOperation;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.TABLE_NOT_FOUND;
import static com.dtsx.astra.cli.operations.db.table.TableTruncateOperation.*;

@Command(
    name = "truncate-table",
    description = "Atomically delete all rows in a table"
)
@Example(
    comment = "Truncate a table in the default keyspace",
    command = "${cli.name} db truncate-table my_db -t my_table"
)
@Example(
    comment = "Truncate a table in a specific keyspace",
    command = "${cli.name} db truncate-table my_db -k my_keyspace -t my_table"
)
public class TableTruncateCmd extends AbstractTableSpecificCmd<TableTruncateResult> {
    @Override
    public final OutputAll execute(Supplier<TableTruncateResult> result) {
        return switch (result.get()) {
            case TableTruncated() -> handleTableTruncated();
            case TableNotFound() -> throwTableNotFound();
        };
    }

    private OutputAll handleTableTruncated() {
        return OutputAll.response("Table %s has been truncated. All rows have been deleted from the table.".formatted(
            ctx.highlight($tableRef.name())
        ));
    }

    private <T> T throwTableNotFound() {
        throw new AstraCliException(TABLE_NOT_FOUND, """
          @|bold,red Error: Table %s does not exist in keyspace %s.|@
        """.formatted(
            $tableRef.name(),
            $keyspaceRef
        ), List.of(
            new Hint("List existing tables:",
                "${cli.name} db list-tables %s -k %s".formatted($dbRef, $keyspaceRef.name()))
        ));
    }

    @Override
    protected Operation<TableTruncateResult> mkOperation() {
        return new TableTruncateOperation(tableGateway, new TableTruncateRequest($tableRef));
    }
}
