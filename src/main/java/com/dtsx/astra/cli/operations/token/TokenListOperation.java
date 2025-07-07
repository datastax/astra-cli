package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenListOperation.TokenInfo;
import com.dtsx.astra.sdk.org.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class TokenListOperation implements Operation<Stream<TokenInfo>> {
    private final TokenGateway tokenGateway;
    private final RoleGateway roleGateway;

    public record TokenInfo(String generatedOn, String clientId, List<String> roleNames, List<String> roleIds) {}

    @Override
    public Stream<TokenInfo> execute() {
        val tokens = tokenGateway.findAll();
        val roleCache = new HashMap<UUID, String>();

        return tokens.map((token) -> {
            val roleNames = token.getRoles().stream()
                .map(UUID::fromString)
                .map((roleId) -> roleCache.computeIfAbsent(roleId, (id) ->
                    roleGateway
                        .tryFindOne(RoleRef.fromId(id))
                        .map(Role::getName)
                        .orElse(id.toString())))
                .toList();

            return new TokenInfo(token.getGeneratedOn(), token.getClientId(), roleNames, token.getRoles());
        });
    }
}
