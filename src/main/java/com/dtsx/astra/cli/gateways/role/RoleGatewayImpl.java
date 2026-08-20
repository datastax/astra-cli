package com.dtsx.astra.cli.gateways.role;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.internal.role.RoleNotFoundException;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.utils.StringUtils;
import com.dtsx.astra.sdk.org.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.*;
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
    public Stream<Role> findAll(List<RoleRef> refs) {
        val usedRefs = new HashSet<RoleRef>();

        val res = findAll()
            .filter((r) -> {
                val found = refs.stream().filter(ref -> ref.fold(
                    id -> r.getId().equals(id.toString()),
                    name -> r.getName().equals(name)
                )).findFirst();

                found.ifPresent(usedRefs::add);
                return found.isPresent();
            })
            .toList();

        if (!usedRefs.containsAll(refs)) {
            val missingRef = refs.stream()
                .filter(ref -> !usedRefs.contains(ref))
                .findFirst()
                .orElseThrow();

            throw new RoleNotFoundException(missingRef);
        }

        return res.stream();
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
