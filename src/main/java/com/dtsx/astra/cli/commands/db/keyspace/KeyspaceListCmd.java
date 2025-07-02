package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.operations.db.keyspace.KeyspaceListOperation.KeyspaceInfo;
import static com.dtsx.astra.cli.operations.db.keyspace.KeyspaceListOperation.KeyspaceListRequest;

@Command(
    name = "list-keyspaces",
    description = "List all keyspaces in the specified database"
)
@Example(
    comment = "List all keyspaces in a database",
    command = "astra db list-keyspaces my_db"
)
public class KeyspaceListCmd extends AbstractKeyspaceCmd<List<KeyspaceInfo>> {
    @Override
    protected KeyspaceListOperation mkOperation() {
        return new KeyspaceListOperation(keyspaceGateway, new KeyspaceListRequest($dbRef));
    }

    @Override
    protected final OutputHuman executeHuman(List<KeyspaceInfo> result) {
        val data = result.stream()
            .map((ks) -> Map.of(
                "Name", mkKeyspaceDisplayName(ks.name(), ks.isDefault())
            ))
            .toList();

        return new ShellTable(data).withColumns("Name");
    }

    @Override
    protected final OutputAll execute(List<KeyspaceInfo> result) {
        val data = result.stream()
            .map((ks) -> Map.of(
                "name", ks.name(),
                "isDefault", ks.isDefault()
            ))
            .toList();

        return new ShellTable(data).withColumns("name", "isDefault");
    }

    private String mkKeyspaceDisplayName(String name, boolean isDefault) {
        return isDefault ? AstraColors.PURPLE_300.use(name + " (in use)") : name;
    }
}
