package com.dtsx.astra.cli.commands.db.misc;

import com.dtsx.astra.cli.commands.db.AbstractDbSpecificCmd;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.misc.EmbeddingProvidersListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;

import static com.dtsx.astra.cli.operations.db.misc.EmbeddingProvidersListOperation.EmbeddingProviderResult;
import static com.dtsx.astra.cli.operations.db.misc.EmbeddingProvidersListOperation.EmbeddingProvidersListRequest;

@Command(
    name = "list-embedding-providers",
    description = "Find all available embedding providers for a given database"
)
@Example(
    comment = "Find all available embedding providers for a database",
    command = "astra db list-embedding-providers my_db"
)
public class EmbeddingProvidersListCmd extends AbstractDbSpecificCmd<EmbeddingProviderResult> {
    @Override
    protected final OutputJson executeJson(EmbeddingProviderResult result) {
        return OutputJson.serializeValue(result.raw());
    }

    @Override
    protected final OutputAll execute(EmbeddingProviderResult result) {
        val data = result.embeddingProviders()
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

    @Override
    protected EmbeddingProvidersListOperation mkOperation() {
        return new EmbeddingProvidersListOperation(dbGateway, new EmbeddingProvidersListRequest($dbRef));
    }
}
