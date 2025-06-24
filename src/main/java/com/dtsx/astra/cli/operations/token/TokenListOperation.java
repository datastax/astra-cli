package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class TokenListOperation {
    private final TokenGateway tokenGateway;
    private final RoleGateway roleGateway;

    public record TokenInfo(String generatedOn, String clientId, String role) {}

    public List<TokenInfo> execute() {
        val tokens = tokenGateway.findAll();
        val roles = new HashMap<String, String>();
        val rows = new ArrayList<TokenInfo>();

        tokens.forEach(token -> {
            for (int i = 0; i < token.getRoles().size(); i++) {
                val roleId = token.getRoles().get(i);
                roles.computeIfAbsent(roleId, k -> roleGateway.tryFindOne(k)
                    .map(role -> role.getName())
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
