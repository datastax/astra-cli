package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.db.keyspace.KeyspaceListOperation.KeyspaceInfo;
import static com.dtsx.astra.cli.operations.db.keyspace.KeyspaceListOperation.KeyspaceListRequest;
import static com.dtsx.astra.cli.utils.Collectionutils.sequencedMapOf;

@Command(
    name = "list-keyspaces",
    description = "List all keyspaces in the specified database"
)
@Example(
    comment = "List all keyspaces in a database",
    command = "${cli.name} db list-keyspaces my_db"
)
public class KeyspaceListCmd extends AbstractKeyspaceCmd<List<KeyspaceInfo>> {
    @Override
    protected KeyspaceListOperation mkOperation() {
        return new KeyspaceListOperation(keyspaceGateway, new KeyspaceListRequest($dbRef));
    }

    @Override
    protected final OutputHuman executeHuman(Supplier<List<KeyspaceInfo>> result) {
        val data = result.get().stream()
            .map((ks) -> sequencedMapOf(
                "Name", mkKeyspaceDisplayName(ks.name(), ks.isDefault())
            ))
            .toList();

        return new ShellTable(data).withColumns("Name");
    }

    @Override
    protected final OutputAll execute(Supplier<List<KeyspaceInfo>> result) {
        val data = result.get().stream()
            .map((ks) -> sequencedMapOf(
                "name", ks.name(),
                "isDefault", ks.isDefault()
            ))
            .toList();

        return new ShellTable(data).withColumns("name", "isDefault");
    }

    private String mkKeyspaceDisplayName(String name, boolean isDefault) {
        return isDefault ? ctx.colors().PURPLE_300.use(name + " (default)") : name;
    }
}
