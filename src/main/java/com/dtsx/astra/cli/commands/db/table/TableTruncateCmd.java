package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableTruncateOperation;
import lombok.val;
import picocli.CommandLine.Command;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.COLLECTION_NOT_FOUND;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.table.TableTruncateOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "truncate-table",
    description = "Atomically delete all rows in a table"
)
@Example(
    comment = "Truncate a table in the default keyspace",
    command = "astra db truncate-table my_db -c my_table"
)
@Example(
    comment = "Truncate a table in a specific keyspace",
    command = "astra db truncate-table my_db -k my_keyspace -c my_table"
)
public class TableTruncateCmd extends AbstractTableSpecificCmd<TableTruncateResult> {
    @Override
    public final OutputAll execute(TableTruncateResult result) {
        val message = switch (result) {
            case TableTruncated() -> handleTableTruncated();
            case TableNotFound() -> throwTableNotFound();
        };
        
        return OutputAll.message(trimIndent(message));
    }

    private String handleTableTruncated() {
        return """
          Table %s has been truncated in keyspace %s.
          
          All data in the table has been permanently deleted.
          
          %s
          %s
        """.formatted(
            highlight($tableRef.name()),
            highlight($keyspaceRef),
            renderComment("List tables in this keyspace:"),
            renderCommand("astra db list-tables %s -k %s".formatted($dbRef, $keyspaceRef.name()))
        );
    }

    private String throwTableNotFound() {
        throw new AstraCliException(COLLECTION_NOT_FOUND, """
          @|bold,red Error: Table %s does not exist in keyspace %s.|@
          
          %s
          %s
          
          %s
          %s
        """.formatted(
            $tableRef.name(),
            $keyspaceRef,
            renderComment("List existing tables in this keyspace:"),
            renderCommand("astra db list-tables %s -k %s".formatted($dbRef, $keyspaceRef.name())),
            renderComment("List tables in all keyspaces:"),
            renderCommand("astra db list-tables %s --all".formatted($dbRef))
        ));
    }

    @Override
    protected Operation<TableTruncateResult> mkOperation() {
        return new TableTruncateOperation(tableGateway, new TableTruncateRequest($tableRef));
    }

}
