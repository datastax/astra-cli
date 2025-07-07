package com.dtsx.astra.cli.gateways.role;

import com.dtsx.astra.cli.core.exceptions.internal.role.RoleNotFoundException;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.utils.StringUtils;
import com.dtsx.astra.sdk.org.domain.Role;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class RoleGatewayImpl implements RoleGateway {
    private final APIProvider apiProvider;

    @Override
    public Stream<Role> findAll() {
        return AstraLogger.loading("Finding all roles", (_) -> apiProvider.astraOpsClient().roles().findAll());
    }

    @Override
    public Optional<Role> tryFindOne(RoleRef role) {
        return AstraLogger.loading("Looking up role " + highlight(role), (_) -> role.fold(
            id -> apiProvider.astraOpsClient().roles().find(id.toString()),
            name -> apiProvider.astraOpsClient().roles().findByName(StringUtils.removeQuotesIfAny(name))
        ));
    }

    @Override
    public Role findOne(RoleRef role) {
        return tryFindOne(role).orElseThrow(() -> new RoleNotFoundException(role));
    }
}
