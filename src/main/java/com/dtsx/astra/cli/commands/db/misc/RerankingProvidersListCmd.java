package com.dtsx.astra.cli.commands.db.misc;

import com.datastax.astra.client.databases.commands.results.FindRerankingProvidersResult;
import com.dtsx.astra.cli.commands.db.AbstractPromptForDbCmd;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.PlatformChars;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.misc.RerankingProvidersListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.db.misc.RerankingProvidersListOperation.RerankingProviderResult;
import static com.dtsx.astra.cli.operations.db.misc.RerankingProvidersListOperation.RerankingProvidersListRequest;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "list-reranking-providers",
    description = "Find all available reranking providers for a given database"
)
@Example(
    comment = "Find all available reranking providers for a database",
    command = "${cli.name} db list-reranking-providers my_db"
)
public class RerankingProvidersListCmd extends AbstractPromptForDbCmd<RerankingProviderResult> {
    @Override
    protected final OutputJson executeJson(Supplier<RerankingProviderResult> result) {
        val deterministicallyOrderedResult = new FindRerankingProvidersResult(
            result.get().raw().getRerankingProviders().entrySet().stream()
                .peek(e -> e.getValue().setSupportedAuthentication(new TreeMap<>(e.getValue().getSupportedAuthentication())))
                .collect(TreeMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll)
        );

        return OutputJson.serializeValue(deterministicallyOrderedResult);
    }

    @Override
    protected final OutputAll execute(Supplier<RerankingProviderResult> result) {
        val data = result.get().rerankingProviders()
            .map(r -> sequencedMapOf(
                "Key", r.key(),
                "Display Name", r.displayName().orElse("n/a"),
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
    protected RerankingProvidersListOperation mkOperation() {
        return new RerankingProvidersListOperation(dbGateway, new RerankingProvidersListRequest($dbRef));
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to list reranking providers for";
    }
}
