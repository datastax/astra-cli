package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.TABLE_NOT_FOUND;
import static com.dtsx.astra.cli.operations.db.table.TableDeleteOperation.*;
import static com.dtsx.astra.cli.utils.Collectionutils.sequencedMapOf;

@Command(
    name = "delete-table",
    description = "Delete an existing table from the specified database and keyspace"
)
@Example(
    comment = "Delete a table",
    command = "${cli.name} db delete-table my_db -t my_table"
)
@Example(
    comment = "Delete a table from a non-default keyspace",
    command = "${cli.name} db delete-table my_db -k my_keyspace -t my_table"
)
@Example(
    comment = "Delete a table without failing if it doesn't exist",
    command = "${cli.name} db delete-table my_db -t my_table --if-exists"
)
public class TableDeleteCmd extends AbstractTableSpecificCmd<TableDeleteResult> {
    @Option(
        names = { "--if-exists" },
        description = "Do not fail if table does not exist",
        defaultValue = "false"
    )
    public boolean $ifExists;

    @Override
    public final OutputAll execute(Supplier<TableDeleteResult> result) {
        return switch (result.get()) {
            case TableNotFound() -> handleTableNotFound();
            case TableIllegallyNotFound() -> throwTableNotFound();
            case TableDeleted() -> handleTableDeleted();
        };
    }

    private OutputAll handleTableNotFound() {
        val message = "Table %s does not exist in keyspace %s; nothing to delete.".formatted(
            ctx.highlight($tableRef.name()),
            ctx.highlight($keyspaceRef)
        );

        val data = mkData(false);

        return OutputAll.response(message, data, List.of(
            new Hint("List all tables in this keyspace:", "${cli.name} db list-tables %s -k %s".formatted($dbRef, $keyspaceRef.name())),
            new Hint("List all tables in all keyspaces:", "${cli.name} db list-tables %s --all".formatted($dbRef))
        ));
    }

    private OutputAll handleTableDeleted() {
        val message = "Table %s has been deleted from keyspace %s.".formatted(
            ctx.highlight($tableRef.name()),
            ctx.highlight($keyspaceRef)
        );

        val data = mkData(true);

        return OutputAll.response(message, data, List.of(
            new Hint("List remaining tables in this keyspace:", "${cli.name} db list-tables %s -k %s".formatted($dbRef, $keyspaceRef.name()))
        ));
    }

    private <T> T throwTableNotFound() {
        throw new AstraCliException(TABLE_NOT_FOUND, """
          @|bold,red Error: Table %s does not exist in keyspace %s.|@
  
          To ignore this error, provide the @'!--if-exists!@ flag to skip this error if the table doesn't exist.
        """.formatted(
            $tableRef.name(),
            $keyspaceRef
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-exists"),
            new Hint("List existing tables:", "${cli.name} db list-tables %s -k %s".formatted($dbRef, $keyspaceRef.name()))
        ));
    }

    private LinkedHashMap<String, Object> mkData(Boolean wasDeleted) {
        return sequencedMapOf(
            "wasDeleted", wasDeleted
        );
    }

    @Override
    protected Operation<TableDeleteResult> mkOperation() {
        return new TableDeleteOperation(tableGateway, new TableDeleteRequest($tableRef, $ifExists));
    }
}
