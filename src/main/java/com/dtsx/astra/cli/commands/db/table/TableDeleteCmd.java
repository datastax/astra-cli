package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.COLLECTION_NOT_FOUND;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.table.TableDeleteOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "delete-table",
    description = "Delete an existing table from the specified database and keyspace"
)
@Example(
    comment = "Delete a table",
    command = "astra db delete-table my_db -c my_table"
)
@Example(
    comment = "Delete a table from a non-default keyspace",
    command = "astra db delete-table my_db -k my_keyspace -c my_table"
)
@Example(
    comment = "Delete a table without failing if it doesn't exist",
    command = "astra db delete-table my_db -c my_table --if-exists"
)
public class TableDeleteCmd extends AbstractTableSpecificCmd<TableDeleteResult> {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if table does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean $ifExists;

    @Override
    public final OutputAll execute(TableDeleteResult result) {
        val message = switch (result) {
            case TableNotFound() -> handleTableNotFound();
            case TableIllegallyNotFound() -> throwTableNotFound();
            case TableDeleted() -> handleTableDeleted();
        };
        
        return OutputAll.message(trimIndent(message));
    }

    private String handleTableNotFound() {
        return """
          Table %s does not exist in keyspace %s; nothing to delete.
          
          %s
          %s
          
          %s
          %s
        """.formatted(
            highlight($tableRef.name()),
            highlight($keyspaceRef),
            renderComment("List all tables in this keyspace:"),
            renderCommand("astra db list-tables %s -k %s".formatted($dbRef, $keyspaceRef.name())),
            renderComment("List all tables in all keyspaces:"),
            renderCommand("astra db list-tables %s --all".formatted($dbRef))
        );
    }

    private String handleTableDeleted() {
        return """
          Table %s has been deleted from keyspace %s.
          
          %s
          %s
        """.formatted(
            highlight($tableRef.name()),
            highlight($keyspaceRef),
            renderComment("List remaining tables in this keyspace:"),
            renderCommand("astra db list-tables %s -k %s".formatted($dbRef, $keyspaceRef.name()))
        );
    }

    private String throwTableNotFound() {
        throw new AstraCliException(COLLECTION_NOT_FOUND, """
          @|bold,red Error: Table %s does not exist in keyspace %s.|@
          
          To ignore this error, provide the %s flag to skip this error if the table doesn't exist.
          
          %s
          %s
          
          %s
          %s
        """.formatted(
            $tableRef.name(),
            $keyspaceRef,
            highlight("--if-exists"),
            renderComment("Example fix:"),
            renderCommand(originalArgs(), "--if-exists"),
            renderComment("List existing tables:"),
            renderCommand("astra db list-tables %s -k %s".formatted($dbRef, $keyspaceRef.name()))
        ));
    }

    @Override
    protected Operation<TableDeleteResult> mkOperation() {
        return new TableDeleteOperation(tableGateway, new TableDeleteRequest($tableRef, $ifExists));
    }

}
