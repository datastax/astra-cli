package com.dtsx.astra.cli.commands.db.misc;

import com.dtsx.astra.cli.commands.db.AbstractDbSpecificCmd;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.misc.EmbeddingProvidersListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.operations.db.misc.EmbeddingProvidersListOperation.*;

@Command(
    name = "list-embedding-providers"
)
public class EmbeddingProvidersListCmd extends AbstractDbSpecificCmd<List<EmbeddingProviderResult>> {
    @Override
    protected EmbeddingProvidersListOperation mkOperation() {
        return new EmbeddingProvidersListOperation(dbGateway, new EmbeddingProvidersListRequest(dbRef));
    }

    @Override
    protected final OutputAll execute(List<EmbeddingProviderResult> result) {
        val data = result.stream()
            .map(r -> Map.of(
                "Key", r.key(),
                "Display Name", r.displayName().orElse("N/A"),
                "Models", String.valueOf(r.modelsCount()),
                "Parameters", String.valueOf(r.parametersCount()),
                "Auth Header", r.hasAuthHeader() ? "■" : "",
                "Auth Secret", r.hasAuthSecret() ? "■" : ""
            ))
            .toList();

        return new ShellTable(data).withColumns(
            "Key", "Display Name", "Models", "Parameters", "Auth Header", "Auth Secret"
        );
    }
}
