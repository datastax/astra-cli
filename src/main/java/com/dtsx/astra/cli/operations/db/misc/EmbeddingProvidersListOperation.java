package com.dtsx.astra.cli.operations.db.misc;

import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.operations.db.misc.EmbeddingProvidersListOperation.*;

@RequiredArgsConstructor
public class EmbeddingProvidersListOperation implements Operation<EmbeddingProviderResult> {
    private final DbGateway dbGateway;
    private final EmbeddingProvidersListRequest request;

    public record EmbeddingProviderResult(
        Stream<EmbeddingProviderInfo> embeddingProviders,
        FindEmbeddingProvidersResult raw
    ) {}

    public record EmbeddingProviderInfo(
        String key,
        Optional<String> displayName,
        int modelsCount,
        int parametersCount,
        boolean hasAuthHeader,
        boolean hasAuthSecret
    ) {}

    public record EmbeddingProvidersListRequest(DbRef dbRef) {}

    @Override
    public EmbeddingProviderResult execute() {
        val raw = dbGateway.findEmbeddingProviders(request.dbRef);
        val embeddingProviders = raw.getEmbeddingProviders();

        val info = embeddingProviders.entrySet().stream()
            .map((entry) -> {
                val provider = entry.getValue();

                return new EmbeddingProviderInfo(
                    entry.getKey(),
                    Optional.ofNullable(provider.getDisplayName()),
                    provider.getModels().size(),
                    provider.getParameters().size(),
                    provider.getHeaderAuthentication().isPresent(),
                    provider.getSharedSecretAuthentication().isPresent()
                );
            });

        return new EmbeddingProviderResult(info, raw);
    }
}
