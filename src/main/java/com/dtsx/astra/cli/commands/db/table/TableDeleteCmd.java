package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.core.output.ExitCode.COLLECTION_NOT_FOUND;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.table.TableDeleteOperation.*;

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
        return switch (result) {
            case TableNotFound() -> handleTableNotFound();
            case TableIllegallyNotFound() -> throwTableNotFound();
            case TableDeleted() -> handleTableDeleted();
        };
    }

    private OutputAll handleTableNotFound() {
        val message = "Table %s does not exist in keyspace %s; nothing to delete.".formatted(
            highlight($tableRef.name()),
            highlight($keyspaceRef)
        );

        val data = mkData(false);

        return OutputAll.response(message, data, List.of(
            new Hint("List all tables in this keyspace:", "astra db list-tables %s -k %s".formatted($dbRef, $keyspaceRef.name())),
            new Hint("List all tables in all keyspaces:", "astra db list-tables %s --all".formatted($dbRef))
        ));
    }

    private OutputAll handleTableDeleted() {
        val message = "Table %s has been deleted from keyspace %s.".formatted(
            highlight($tableRef.name()),
            highlight($keyspaceRef)
        );

        val data = mkData(true);

        return OutputAll.response(message, data, List.of(
            new Hint("List remaining tables in this keyspace:", "astra db list-tables %s -k %s".formatted($dbRef, $keyspaceRef.name()))
        ));
    }

    private <T> T throwTableNotFound() {
        throw new AstraCliException(COLLECTION_NOT_FOUND, """
          @|bold,red Error: Table %s does not exist in keyspace %s.|@
  
          To ignore this error, provide the @!--if-exists!@ flag to skip this error if the table doesn't exist.
        """.formatted(
            $tableRef.name(),
            $keyspaceRef
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-exists"),
            new Hint("List existing tables:", "astra db list-tables %s -k %s".formatted($dbRef, $keyspaceRef.name()))
        ));
    }

    private Map<String, Object> mkData(Boolean wasDeleted) {
        return Map.of(
            "wasDeleted", wasDeleted
        );
    }

    @Override
    protected Operation<TableDeleteResult> mkOperation() {
        return new TableDeleteOperation(tableGateway, new TableDeleteRequest($tableRef, $ifExists));
    }
}
