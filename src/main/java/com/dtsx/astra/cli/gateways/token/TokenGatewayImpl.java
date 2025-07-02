package com.dtsx.astra.cli.gateways.token;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import com.dtsx.astra.sdk.org.domain.IamToken;
import com.dtsx.astra.sdk.org.domain.Role;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class TokenGatewayImpl implements TokenGateway {
    private final APIProvider apiProvider;

    @Override
    public Stream<IamToken> findAll() {
        return AstraLogger.loading("Fetching tokens for the current org", (_) -> 
            apiProvider.astraOpsClient().tokens().findAll());
    }

    @Override
    public Optional<IamToken> tryFindOne(String clientId) {
        return apiProvider.astraOpsClient().tokens().findAll()
                .filter(t -> t.getClientId().equals(clientId))
                .findFirst();
    }

    @Override
    public boolean exists(String clientId) {
        return apiProvider.astraOpsClient().tokens().exist(clientId);
    }

    @Override
    public CreateTokenResponse create(Role role) {
        return AstraLogger.loading("Creating token with role " + highlight(role.getName()), (_) -> 
            apiProvider.astraOpsClient().tokens().create(role.getId()));
    }

    @Override
    public DeletionStatus<Void> delete(String clientId) {
        if (!exists(clientId)) {
            return DeletionStatus.notFound(null);
        }
        
        AstraLogger.loading("Deleting token " + highlight(clientId), (_) -> {
            apiProvider.astraOpsClient().tokens().delete(clientId);
            return null;
        });
        
        return DeletionStatus.deleted(null);
    }
}
