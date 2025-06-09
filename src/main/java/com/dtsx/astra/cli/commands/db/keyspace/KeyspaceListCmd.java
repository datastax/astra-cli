package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.output.output.OutputAll;
import com.dtsx.astra.cli.output.table.ShellTable;
import lombok.val;
import picocli.CommandLine.*;

import java.util.Map;

@Command(
    name = "list-keyspaces"
)
public class KeyspaceListCmd extends AbstractKeyspaceCmd {
    @Override
    protected OutputAll execute() {
        val result = keyspaceService.listKeyspaces(dbRef);

        val data = result.keyspaces().stream()
            .map((ks) -> Map.of("Name", ks.equals(result.defaultKeyspace()) ? ShellTable.highlight(ks + " (default)") : ks))
            .toList();

        return new ShellTable(data).withColumns("Name");
    }
}
