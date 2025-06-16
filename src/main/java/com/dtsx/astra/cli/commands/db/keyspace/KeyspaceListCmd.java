package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceListOperation;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import lombok.val;
import picocli.CommandLine.*;

import java.util.Map;

@Command(
    name = "list-keyspaces"
)
public final class KeyspaceListCmd extends AbstractKeyspaceCmd {
    @Override
    protected OutputAll execute() {
        val keyspaces = new KeyspaceListOperation(keyspaceGateway).execute(dbRef);

        val data = keyspaces.stream()
            .map((ks) -> Map.of("Name", mkKeyspaceDisplayName(ks.name(), ks.isDefault())))
            .toList();

        return new ShellTable(data).withColumns("Name");
    }

    private String mkKeyspaceDisplayName(String name, boolean isDefault) {
        return isDefault ? AstraColors.PURPLE_300.use(name + " (in use)") : name;
    }
}
