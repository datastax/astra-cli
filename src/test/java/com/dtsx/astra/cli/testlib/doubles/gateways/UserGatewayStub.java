package com.dtsx.astra.cli.testlib.doubles.gateways;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.sdk.org.domain.User;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.testlib.doubles.Utils.methodIllegallyCalled;

public class UserGatewayStub implements UserGateway {
    @Override
    public User findOne(UserRef user) {
        return methodIllegallyCalled();
    }

    @Override
    public Stream<User> findAll() {
        return methodIllegallyCalled();
    }

    @Override
    public CreationStatus<List<UUID>> invite(UserRef user, List<RoleRef> roles) {
        return methodIllegallyCalled();
    }

    @Override
    public DeletionStatus<Void> delete(UserRef user) {
        return methodIllegallyCalled();
    }
}
