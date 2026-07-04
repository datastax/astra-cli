package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenListOperation.TokenInfo;
import com.dtsx.astra.sdk.org.domain.IamToken;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;

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
        List<@Nullable UUID> roleIds,
        IamToken raw
    ) {}

    @Override
    public Stream<TokenInfo> execute() {
        val tokens = tokenGateway.findAll().toList();

        val roleMappings = roleGateway.findNames(
            tokens.stream()
                .flatMap((token) -> token.getRoles().stream())
                .map((nameOrId) -> {
                    try {
                        return UUID.fromString(nameOrId);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toSet())
        );

        return tokens.stream().map((token) -> {
            val roles = token.getRoles().stream()
                .map((nameOrId) -> {
                    try {
                        return Either.<String, UUID>pure(UUID.fromString(nameOrId));
                    } catch (Exception e) {
                        return Either.<String, UUID>left(nameOrId);
                    }
                })
                .toList();

            val roleIds = roles.stream()
                .map((either) -> either.fold(_ -> null, id -> id))
                .toList();

            val roleNames = roles.stream()
                .map((either) -> either.fold(
                    name -> name,
                    roleMappings::get
                ))
                .toList();

            return new TokenInfo(roleNames, roleIds, token);
        });
    }
}
