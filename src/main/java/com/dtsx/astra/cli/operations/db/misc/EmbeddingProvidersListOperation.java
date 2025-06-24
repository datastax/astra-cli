package com.dtsx.astra.cli.operations.db.misc;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.operations.db.misc.EmbeddingProvidersListOperation.*;

@RequiredArgsConstructor
public class EmbeddingProvidersListOperation implements Operation<List<EmbeddingProviderResult>> {
    private final DbGateway dbGateway;
    private final EmbeddingProvidersListRequest request;

    public record EmbeddingProviderResult(
        String key,
        Optional<String> displayName,
        int modelsCount,
        int parametersCount,
        boolean hasAuthHeader,
        boolean hasAuthSecret
    ) {}

    public record EmbeddingProvidersListRequest(DbRef dbRef) {}

    @Override
    public List<EmbeddingProviderResult> execute() {
        val result = dbGateway.findEmbeddingProviders(request.dbRef);
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
