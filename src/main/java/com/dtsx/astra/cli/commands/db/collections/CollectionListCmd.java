package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.operations.collection.CollectionListOperation;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;

@Command(
    name = "list-collections"
)
public class CollectionListCmd extends AbstractCollectionCmd {
    private CollectionListOperation collectionListOperation;

    @Override
    protected void prelude() {
        super.prelude();
        this.collectionListOperation = new CollectionListOperation(collectionGateway);
    }

    @Override
    protected OutputAll execute() {
        val result = collectionListOperation.execute(keyspaceRef);

        val data = result.collections().stream()
            .map((coll) -> Map.of("Name", coll))
            .toList();

        return new ShellTable(data).withColumns("Name");
    }
}
