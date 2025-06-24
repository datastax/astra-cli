package com.dtsx.astra.cli.operations.db.misc;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class EmbeddingProvidersListOperation {
    private final DbGateway dbGateway;

    public record EmbeddingProviderResult(
        String key,
        Optional<String> displayName,
        int modelsCount,
        int parametersCount,
        boolean hasAuthHeader,
        boolean hasAuthSecret
    ) {}

    public List<EmbeddingProviderResult> execute(DbRef dbRef) {
        val result = dbGateway.findEmbeddingProviders(dbRef);
        val embeddingProviders = result.getEmbeddingProviders();

        return embeddingProviders.entrySet().stream()
            .map((entry) -> {
                val provider = entry.getValue();
                return new EmbeddingProviderResult(
                    entry.getKey(),
                    Optional.ofNullable(provider.getDisplayName()),
                    provider.getModels().size(),
                    provider.getParameters().size(),
                    provider.getHeaderAuthentication().isPresent(),
                    provider.getSharedSecretAuthentication().isPresent()
                );
            })
            .toList();
    }
}
