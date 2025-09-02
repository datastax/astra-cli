package com.dtsx.astra.cli.testlib.doubles.gateways;

import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.sdk.org.domain.Role;

import java.util.Optional;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.testlib.doubles.Utils.methodIllegallyCalled;

public class RoleGatewayStub extends GatewayStub implements RoleGateway {
    @Override
    public Stream<Role> findAll() {
        return methodIllegallyCalled();
    }

    @Override
    public Optional<Role> tryFindOne(RoleRef ref) {
        return methodIllegallyCalled();
    }
}
