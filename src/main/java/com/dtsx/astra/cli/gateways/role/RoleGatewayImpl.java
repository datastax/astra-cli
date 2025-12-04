package com.dtsx.astra.cli.gateways.role;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.utils.StringUtils;
import com.dtsx.astra.sdk.org.domain.Role;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class RoleGatewayImpl implements RoleGateway {
    private final CliContext ctx;
    private final APIProvider apiProvider;

    @Override
    public Stream<Role> findAll() {
        return ctx.log().loading("Finding all roles", (_) -> apiProvider.astraOpsClient().roles().findAll());
    }

    @Override
    public Optional<Role> tryFindOne(RoleRef ref) {
        return ctx.log().loading("Looking up role " + ctx.highlight(ref), (_) -> ref.fold(
            id -> apiProvider.astraOpsClient().roles().find(id.toString()),
            name -> apiProvider.astraOpsClient().roles().findByName(StringUtils.removeQuotesIfAny(name))
        ));
    }

    @Override
    public Map<UUID, Optional<String>> findNames(Set<UUID> ids) {
        return ids.stream()
            .collect(Collectors.toMap(
                id -> id,
                id -> ctx.log().loading("Looking up role name for " + ctx.highlight(id), (_) ->
                    tryFindOne(RoleRef.fromId(id)).map(Role::getName)
                )
            ));
    }
}
