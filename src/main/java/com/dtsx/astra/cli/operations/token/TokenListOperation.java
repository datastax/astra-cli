package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenListOperation.TokenInfo;
import com.dtsx.astra.sdk.org.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
public class TokenListOperation implements Operation<List<TokenInfo>> {
    private final TokenGateway tokenGateway;
    private final RoleGateway roleGateway;

    public record TokenInfo(String generatedOn, String clientId, String role) {}

    @Override
    public List<TokenInfo> execute() {
        val tokens = tokenGateway.findAll();
        val roles = new HashMap<String, String>();
        val rows = new ArrayList<TokenInfo>();

        tokens.forEach(token -> {
            for (int i = 0; i < token.getRoles().size(); i++) {
                val roleId = token.getRoles().get(i);
                roles.computeIfAbsent(roleId, k -> roleGateway.tryFindOne(k)
                    .map(Role::getName)
                    .orElse(k));

                val generatedOn = (i == 0) ? token.getGeneratedOn() : "";
                val clientId = (i == 0) ? token.getClientId() : "";
                val roleName = roles.get(roleId);

                rows.add(new TokenInfo(generatedOn, clientId, roleName));
            }
        });

        return rows;
    }
}
