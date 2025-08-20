package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableListOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.operations.db.table.TableListOperation.*;

@Command(
    name = "list-tables",
    description = "List the tables in the specified database and keyspace"
)
@Example(
    comment = "List all tables in the default keyspace",
    command = "astra db list-tables my_db"
)
@Example(
    comment = "List all tables in a specific keyspace",
    command = "astra db list-tables my_db -k my_keyspace"
)
@Example(
    comment = "List all tables in all keyspaces",
    command = "astra db list-tables my_db --all"
)
public class TableListCmd extends AbstractTableCmd<Stream<TableListResult>> {
    @Option(
        names = { "--all", "-a" },
        description = "List tables in all keyspaces",
        defaultValue = "false"
    )
    public boolean $all;

    @Override
    protected OutputJson executeJson(Supplier<Stream<TableListResult>> result) {
        validateParams();

        if ($all) {
            return OutputJson.serializeValue(result.get()
                .map(res -> Map.of(
                    "keyspace", res.keyspace(),
                    "tables", res.tables()
                ))
                .toList());
        }

        return OutputJson.serializeValue(result.get().toList().getFirst().tables());
    }

    @Override
    public final OutputAll execute(Supplier<Stream<TableListResult>> result) {
        validateParams();

        val data = result.get()
            .flatMap((res) -> (
                res.tables().stream()
                    .map(desc -> Map.of(
                        "Keyspace", res.keyspace(),
                        "Name", desc.getName()
                    ))
            ))
            .toList();

        if ($all) {
            return new ShellTable(data).withColumns("Keyspace", "Name");
        } else {
            return new ShellTable(data).withColumns("Name");
        }
    }

    private void validateParams() {
        if ($all && !$keyspaceRef.isDefaultKeyspace()) {
            throw new ParameterException(spec.commandLine(), "Cannot use --all with a specific keyspace (the -k flag)");
        }
    }

    @Override
    protected Operation<Stream<TableListResult>> mkOperation() {
        return new TableListOperation(tableGateway, keyspaceGateway, new TableListRequest($keyspaceRef, $all));
    }
}
