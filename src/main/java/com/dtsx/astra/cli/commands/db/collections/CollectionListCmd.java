package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.collection.CollectionListOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.util.Map;

@Command(
    name = "list-collections"
)
public final class CollectionListCmd extends AbstractCollectionCmd {
    @Option(
        names = { "--all", "-a" },
        description = "List collections in all keyspaces",
        defaultValue = "false"
    )
    public boolean all;

    @Override
    protected OutputAll execute() {
        if (all && !keyspaceRef.isDefaultKeyspace()) {
            throw new ParameterException(spec.commandLine(), "Cannot use --all with a specific keyspace (the -k flag)");
        }

        val operation = new CollectionListOperation(collectionGateway, keyspaceGateway);
        val result = operation.execute(keyspaceRef, all);

        val data = result.stream()
            .flatMap((res) -> (
                res.collections().stream()
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
