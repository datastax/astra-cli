package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableListOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.operations.db.table.TableListOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

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
public class TableListCmd extends AbstractTableCmd<List<TableListResult>> {
    @Option(
        names = { "--all", "-a" },
        description = "List tables in all keyspaces",
        defaultValue = "false"
    )
    public boolean $all;

    @Override
    public final OutputAll execute(List<TableListResult> result) {
        return handleTableList(result);
    }

    private OutputAll handleTableList(List<TableListResult> result) {
        if ($all && !$keyspaceRef.isDefaultKeyspace()) {
            throw new ParameterException(spec.commandLine(), "Cannot use --all with a specific keyspace (the -k flag)");
        }

        val data = result.stream()
            .flatMap((res) -> (
                res.tables().stream()
                    .map(desc -> Map.of(
                        "Keyspace", res.keyspace(),
                        "Name", desc.getName()
                    ))
            ))
            .toList();

        return $all
            ? new ShellTable(data).withColumns("Keyspace", "Name")
            : new ShellTable(data).withColumns("Name");
    }

    @Override
    protected Operation<List<TableListResult>> mkOperation() {
        return new TableListOperation(tableGateway, keyspaceGateway, new TableListRequest($keyspaceRef, $all));
    }
}
