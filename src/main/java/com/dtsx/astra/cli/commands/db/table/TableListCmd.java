package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.table.TableListOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.util.Map;

@Command(
    name = "list-tables"
)
public final class TableListCmd extends AbstractTableCmd {
    @Option(
        names = { "--all", "-a" },
        description = "List tables in all keyspaces",
        defaultValue = "false"
    )
    public boolean all;

    @Override
    protected OutputAll execute() {
        if (all && !keyspaceRef.isDefaultKeyspace()) {
            throw new ParameterException(spec.commandLine(), "Cannot use --all with a specific keyspace (the -k flag)");
        }

        val operation = new TableListOperation(tableGateway, keyspaceGateway);
        val result = operation.execute(keyspaceRef, all);

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
}
