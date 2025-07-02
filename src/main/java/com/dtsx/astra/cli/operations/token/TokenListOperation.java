package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenListOperation.TokenInfo;
import com.dtsx.astra.sdk.org.domain.IamToken;
import com.dtsx.astra.sdk.org.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class TokenListOperation implements Operation<Stream<TokenInfo>> {
    private final TokenGateway tokenGateway;
    private final RoleGateway roleGateway;

    public record TokenInfo(String generatedOn, String clientId, List<String> roleNames, List<String> roleIds) {}

    @Override
    public Stream<TokenInfo> execute() {
        val tokens = tokenGateway.findAll();
        val roleCache = new HashMap<String, String>();

        return tokens.map((token) -> {
            val roleNames = token.getRoles().stream()
                .map((roleId) -> roleCache.computeIfAbsent(roleId, (k) -> roleGateway.tryFindOne(k)
                    .map(Role::getName)
                    .orElse(k)))
                .toList();

            return new TokenInfo(token.getGeneratedOn(), token.getClientId(), roleNames, token.getRoles());
        });
    }
}
