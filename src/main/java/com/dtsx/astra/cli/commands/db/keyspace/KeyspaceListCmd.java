package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceListOperation;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import lombok.val;
import picocli.CommandLine.*;

import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.operations.db.keyspace.KeyspaceListOperation.*;

@Command(
    name = "list-keyspaces"
)
public class KeyspaceListCmd extends AbstractKeyspaceCmd<List<KeyspaceInfo>> {
    @Override
    protected KeyspaceListOperation mkOperation() {
        return new KeyspaceListOperation(keyspaceGateway, new KeyspaceListRequest(dbRef));
    }

    @Override
    protected final OutputAll execute(List<KeyspaceInfo> result) {
        val data = result.stream()
            .map((ks) -> Map.of("Name", mkKeyspaceDisplayName(ks.name(), ks.isDefault())))
            .toList();

        return new ShellTable(data).withColumns("Name");
    }

    private String mkKeyspaceDisplayName(String name, boolean isDefault) {
        return isDefault ? AstraColors.PURPLE_300.use(name + " (in use)") : name;
    }
}
