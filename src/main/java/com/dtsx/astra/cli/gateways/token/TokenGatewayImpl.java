package com.dtsx.astra.cli.gateways.token;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.internal.misc.InvalidTokenException;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.sdk.exception.AuthenticationException;
import com.dtsx.astra.sdk.org.domain.CreateTokenRequest;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import com.dtsx.astra.sdk.org.domain.IamToken;
import com.dtsx.astra.sdk.org.domain.Role;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public class TokenGatewayImpl implements TokenGateway {
    private final CliContext ctx;
    private final APIProvider apiProvider;
    private final RoleGateway roleGateway;
    private final OrgGateway orgGateway;

    @Override
    public Stream<IamToken> findAll() {
        return ctx.log().loading("Fetching tokens for the current org", (_) -> apiProvider.astraOpsClient().tokens().findAll());
    }

    @Override
    public boolean exists(String clientId) {
        return apiProvider.astraOpsClient().tokens().exist(clientId);
    }

    @Override
    public CreateTokenResponse create(NEList<RoleRef> refs, Optional<String> description, Optional<UUID> orgId, Optional<Instant> expiration) {
        val roles = roleGateway.findAll(refs).toList();

        val roleDesc = (roles.size() == 1)
            ? "role " + ctx.highlight(roles.getFirst().getName())
            : ctx.highlight(roles.size()) + " roles";

        return ctx.log().loading("Creating token with " + roleDesc, (_) -> {
            val req = new CreateTokenRequest();
            req.setRoles(roles.stream().map(Role::getId).collect(toSet()));
            req.setDescription(description.orElse(null));
            req.setOrgId(orgId.orElse(null));
            req.setExpirationDate(expiration.orElse(null));
            return apiProvider.astraOpsClient().tokens().create(req);
        });
    }

    @Override
    public DeletionStatus<Void> delete(String clientId) {
        if (!exists(clientId)) {
            return DeletionStatus.notFound(null);
        }
        
        ctx.log().loading("Deleting token " + ctx.highlight(clientId), (_) -> {
            apiProvider.astraOpsClient().tokens().delete(clientId);
            return null;
        });
        
        return DeletionStatus.deleted(null);
    }

    @Override
    public void validate(AstraToken token, AstraEnvironment env) {
        ctx.log().loading("Validating your Astra token", (_) -> {
            try {
                return orgGateway.find(token, env);
            } catch (AuthenticationException e) {
                throw new InvalidTokenException("could not validate the provided token");
            }
        });
    }
}
