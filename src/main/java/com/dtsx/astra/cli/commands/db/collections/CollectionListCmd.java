package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionListOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.operations.db.collection.CollectionListOperation.CollectionListRequest;
import static com.dtsx.astra.cli.operations.db.collection.CollectionListOperation.CollectionListResult;

@Command(
    name = "list-collections",
    description = "List the collections in the specified database and keyspace"
)
@Example(
    comment = "List all collections in the default keyspace",
    command = "astra db list-collections my_db"
)
@Example(
    comment = "List all collections in a specific keyspace",
    command = "astra db list-collections my_db -k my_keyspace"
)
@Example(
    comment = "List all collections in all keyspaces",
    command = "astra db list-collections my_db --all"
)
public class CollectionListCmd extends AbstractCollectionCmd<Stream<CollectionListResult>> {
    @Option(
        names = { "--all", "-a" },
        description = "List collections in all keyspaces",
        defaultValue = "false"
    )
    public boolean $all;

    @Override
    protected OutputJson executeJson(Supplier<Stream<CollectionListResult>> result) {
        validateParams();

        if ($all) {
            return OutputJson.serializeValue(result.get().toList().getFirst().collections());
        }

        return OutputJson.serializeValue(result.get()
            .map(res -> Map.of(
                "keyspace", res.keyspace(),
                "collections", res.collections()
            ))
            .toList());
    }

    @Override
    public final OutputAll execute(Supplier<Stream<CollectionListResult>> result) {
        validateParams();

        val data = result.get()
            .flatMap((res) -> (
                res.collections().stream()
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
    protected Operation<Stream<CollectionListResult>> mkOperation() {
        return new CollectionListOperation(collectionGateway, keyspaceGateway, new CollectionListRequest($keyspaceRef, $all));
    }
}
