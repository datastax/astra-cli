package com.dtsx.astra.cli.gateways.role;

import com.dtsx.astra.cli.core.exceptions.internal.role.RoleNotFoundException;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.org.domain.Role;

import java.util.*;
import java.util.stream.Stream;

public interface RoleGateway extends SomeGateway {
    Stream<Role> findAll();

    Optional<Role> tryFindOne(RoleRef ref);

    default Role findOne(RoleRef ref) {
        return tryFindOne(ref).orElseThrow(() -> new RoleNotFoundException(ref));
    }

    Map<UUID, Optional<String>> findNames(Set<UUID> ids);
}
