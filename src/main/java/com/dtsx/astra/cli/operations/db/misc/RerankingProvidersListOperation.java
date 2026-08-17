package com.dtsx.astra.cli.operations.db.misc;

import com.datastax.astra.client.databases.commands.results.FindRerankingProvidersResult;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.operations.db.misc.RerankingProvidersListOperation.RerankingProviderResult;

@RequiredArgsConstructor
public class RerankingProvidersListOperation implements Operation<RerankingProviderResult> {
    private final DbGateway dbGateway;
    private final RerankingProvidersListRequest request;

    public record RerankingProviderResult(
        Stream<RerankingProviderInfo> rerankingProviders,
        FindRerankingProvidersResult raw
    ) {}

    public record RerankingProviderInfo(
        String key,
        Optional<String> displayName,
        int modelsCount,
        int parametersCount,
        boolean hasAuthHeader,
        boolean hasAuthSecret
    ) {}

    public record RerankingProvidersListRequest(DbRef dbRef) {}

    @Override
    public RerankingProviderResult execute() {
        val raw = dbGateway.findRerankingProviders(request.dbRef);
        val rerankingProviders = raw.getRerankingProviders();

        val info = rerankingProviders.entrySet().stream()
            .map((entry) -> {
                val provider = entry.getValue();

                return new RerankingProviderInfo(
                    entry.getKey(),
                    Optional.ofNullable(provider.getDisplayName()),
                    provider.getModels().size(),
                    provider.getParameters().size(),
                    provider.getHeaderAuthentication().isPresent(),
                    provider.getSharedSecretAuthentication().isPresent()
                );
            });

        return new RerankingProviderResult(info, raw);
    }
}
