package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.output.output.OutputAll;
import com.dtsx.astra.cli.output.table.ShellTable;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;

@Command(
    name = "list-collections"
)
public class CollectionListCmd extends AbstractCollectionCmd {
    @Override
    protected OutputAll execute() {
        val result = collectionService.listCollections(keyspaceRef);

        val data = result.stream()
            .map((coll) -> Map.of("Name", coll))
            .toList();

        return new ShellTable(data).withColumns("Name");
    }
}
