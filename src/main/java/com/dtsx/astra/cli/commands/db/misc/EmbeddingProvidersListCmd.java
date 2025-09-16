package com.dtsx.astra.cli.commands.db.misc;

import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.dtsx.astra.cli.commands.db.AbstractPromptForDbCmd;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.PlatformChars;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.misc.EmbeddingProvidersListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.db.misc.EmbeddingProvidersListOperation.EmbeddingProviderResult;
import static com.dtsx.astra.cli.operations.db.misc.EmbeddingProvidersListOperation.EmbeddingProvidersListRequest;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "list-embedding-providers",
    description = "Find all available embedding providers for a given database"
)
@Example(
    comment = "Find all available embedding providers for a database",
    command = "${cli.name} db list-embedding-providers my_db"
)
public class EmbeddingProvidersListCmd extends AbstractPromptForDbCmd<EmbeddingProviderResult> {
    @Override
    protected final OutputJson executeJson(Supplier<EmbeddingProviderResult> result) {
        val deterministicallyOrderedResult = new FindEmbeddingProvidersResult(
            result.get().raw().getEmbeddingProviders().entrySet().stream()
                .peek(e -> e.getValue().setSupportedAuthentication(new TreeMap<>(e.getValue().getSupportedAuthentication())))
                .collect(TreeMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll)
        );

        return OutputJson.serializeValue(deterministicallyOrderedResult);
    }

    @Override
    protected final OutputAll execute(Supplier<EmbeddingProviderResult> result) {
        val data = result.get().embeddingProviders()
            .map(r -> sequencedMapOf(
                "Key", r.key(),
                "Display Name", r.displayName().orElse("N/A"),
                "Models", String.valueOf(r.modelsCount()),
                "Parameters", String.valueOf(r.parametersCount()),
                "Auth Header", r.hasAuthHeader() ? PlatformChars.presenceIndicator(ctx.isWindows()) : "",
                "Auth Secret", r.hasAuthSecret() ? PlatformChars.presenceIndicator(ctx.isWindows()) : ""
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

    @Override
    protected String dbRefPrompt() {
        return "Select the database to list embedding providers for";
    }
}
