package com.dtsx.astra.cli.gateways.role;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.utils.StringUtils;
import com.dtsx.astra.sdk.org.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

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
    public Map<UUID, String> findNames(Set<UUID> ids) {
        val roles = findAll().collect(toMap(
            r -> {
                try {
                    return UUID.fromString(r.getId());
                } catch (Exception e) {
                    return null;
                }
            },
            Role::getName
        ));

        return ids.stream()
            .filter(roles::containsKey)
            .collect(toMap(
                id -> id,
                roles::get
            ));
    }
}
