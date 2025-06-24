package com.dtsx.astra.cli.commands.db.table;

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

@Command(
    name = "list-tables"
)
public class TableListCmd extends AbstractTableCmd<List<TableListResult>> {
    @Option(
        names = { "--all", "-a" },
        description = "List tables in all keyspaces",
        defaultValue = "false"
    )
    public boolean all;

    @Override
    public final OutputAll execute(List<TableListResult> result) {
        if (all && !keyspaceRef.isDefaultKeyspace()) {
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

        if (all) {
            return new ShellTable(data).withColumns("Keyspace", "Name");
        } else {
            return new ShellTable(data).withColumns("Name");
        }
    }

    @Override
    protected Operation<List<TableListResult>> mkOperation() {
        return new TableListOperation(tableGateway, keyspaceGateway, new TableListRequest(keyspaceRef, all));
    }
}
