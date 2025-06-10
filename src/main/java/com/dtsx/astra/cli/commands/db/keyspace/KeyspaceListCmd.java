package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.operations.keyspace.KeyspaceListOperation;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import lombok.val;
import picocli.CommandLine.*;

import java.util.Map;

@Command(
    name = "list-keyspaces"
)
public class KeyspaceListCmd extends AbstractKeyspaceCmd {
    private KeyspaceListOperation keyspaceListOperation;

    @Override
    protected void prelude() {
        super.prelude();
        this.keyspaceListOperation = new KeyspaceListOperation(keyspaceGateway);
    }

    @Override
    protected OutputAll execute() {
        val result = keyspaceListOperation.execute(dbRef);
        val foundKeyspaces = result.foundKeyspaces();

        val data = foundKeyspaces.keyspaces().stream()
            .map((ks) -> Map.of("Name", ks.equals(foundKeyspaces.defaultKeyspace()) ? ShellTable.highlight(ks + " (default)") : ks))
            .toList();

        return new ShellTable(data).withColumns("Name");
    }
}
