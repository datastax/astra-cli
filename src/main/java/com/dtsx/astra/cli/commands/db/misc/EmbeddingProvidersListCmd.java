package com.dtsx.astra.cli.commands.db.misc;

import com.dtsx.astra.cli.commands.db.AbstractDbSpecificCmd;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.misc.EmbeddingProvidersListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;

@Command(
    name = "list-embedding-providers"
)
public final class EmbeddingProvidersListCmd extends AbstractDbSpecificCmd {
    @Override
    protected OutputAll execute() {
        val results = new EmbeddingProvidersListOperation(dbGateway).execute(dbRef);

        val data = results.stream()
            .map(result -> Map.of(
                "Key", result.key(),
                "Display Name", result.displayName().orElse("N/A"),
                "Models", String.valueOf(result.modelsCount()),
                "Parameters", String.valueOf(result.parametersCount()),
                "Auth Header", result.hasAuthHeader() ? "■" : "",
                "Auth Secret", result.hasAuthSecret() ? "■" : ""
            ))
            .toList();

        return new ShellTable(data).withColumns(
            "Key", "Display Name", "Models", "Parameters", "Auth Header", "Auth Secret"
        );
    }
}
