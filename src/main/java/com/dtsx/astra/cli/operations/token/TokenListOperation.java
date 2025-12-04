package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenListOperation.TokenInfo;
import com.dtsx.astra.sdk.org.domain.IamToken;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public class TokenListOperation implements Operation<Stream<TokenInfo>> {
    private final TokenGateway tokenGateway;
    private final RoleGateway roleGateway;

    public record TokenInfo(
        List<String> roleNames,
        List<UUID> roleIds,
        IamToken raw
    ) {}

    @Override
    public Stream<TokenInfo> execute() {
        val tokens = tokenGateway.findAll().toList();

        val roleMappings = roleGateway.findNames(
            tokens
                .stream()
                .flatMap((token) -> token.getRoles().stream())
                .map(UUID::fromString)
                .collect(toSet())
        );

        return tokens.stream().map((token) -> {
            val roleIds = token.getRoles().stream()
                .map(UUID::fromString)
                .toList();

            val roleNames = roleIds.stream()
                .map((roleId) -> roleMappings.get(roleId).orElse(roleId.toString()))
                .toList();

            return new TokenInfo(roleNames, roleIds, token);
        });
    }
}
