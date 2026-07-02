package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenListOperation.TokenInfo;
import com.dtsx.astra.sdk.org.domain.IamToken;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Objects;
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

//        tokens.forEach((t) -> {
//            try {
//                t.getRoles().forEach(UUID::fromString);
//            } catch (Exception _) {
//                System.out.println(JsonUtils.formatJsonPretty(t));
//            }
//        });

        val roleMappings = roleGateway.findNames(
            tokens
                .stream()
                .flatMap((token) -> token.getRoles().stream())
                .map((id) -> {
                    try {
                        return UUID.fromString(id);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toSet())
        );

        return tokens.stream().map((token) -> {
            val roleIds = token.getRoles().stream()
                .map((id) -> {
                    try {
                        return UUID.fromString(id); // ugly duplication and null usage but whatever it's fine for now
                    } catch (Exception e) {
                        return null;
                    }
                })
                .toList();

            val roleNames = roleIds.stream()
                .map((roleId) -> roleMappings.get(roleId).orElse((roleId != null) ? roleId.toString() : null))
                .toList();

            return new TokenInfo(roleNames, roleIds, token);
        });
    }
}
