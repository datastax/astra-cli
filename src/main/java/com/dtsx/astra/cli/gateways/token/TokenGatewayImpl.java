package com.dtsx.astra.cli.gateways.token;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.internal.misc.InvalidTokenException;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.utils.JsonUtils;
import com.dtsx.astra.sdk.exception.AuthenticationException;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import com.dtsx.astra.sdk.org.domain.IamToken;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class TokenGatewayImpl implements TokenGateway {
    private final CliContext ctx;
    private final APIProvider apiProvider;
    private final RoleGateway roleGateway;
    private final OrgGateway.Stateless statelessOrgGateway;

    @Override
    public Stream<IamToken> findAll() {
        return ctx.log().loading("Fetching tokens for the current org", (_) -> 
            apiProvider.astraOpsClient().tokens().findAll());
    }

    @Override
    public boolean exists(String clientId) {
        return apiProvider.astraOpsClient().tokens().exist(clientId);
    }

    @Override
    public CreateTokenResponse create(RoleRef roleRef, Optional<String> description) {
        val role = roleGateway.findOne(roleRef);

        return ctx.log().loading("Creating token with role " + ctx.highlight(role.getName()), (_) -> {
            val client = apiProvider.astraOpsClient().tokens();

            val body = """
              {
                "roles": ["%s"],
                "description": %s
              }
            """.formatted(
                JsonUtils.escapeJson(role.getId()),
                description.map(d -> '"' + JsonUtils.escapeJson(d) + '"').orElse("null")
            );

            val res = client.POST(client.getEndpointTokens(), body, "tokens.create");


            return JsonUtils.readValue(res.getBody(), CreateTokenResponse.class);
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
    public void validate(AstraToken token) {
        ctx.log().loading("Validating your Astra token", (_) -> {
            try {
                return statelessOrgGateway.resolveOrganizationEnvironment(token);
            } catch (AuthenticationException e) {
                throw new InvalidTokenException("could not authenticate with the provided token");
            }
        });
    }
}
