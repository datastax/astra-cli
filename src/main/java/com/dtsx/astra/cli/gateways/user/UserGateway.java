package com.dtsx.astra.cli.gateways.user;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.org.domain.User;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface UserGateway extends SomeGateway {
    User findOne(UserRef user);

    Stream<User> findAll();

    CreationStatus<List<UUID>> invite(UserRef user, List<RoleRef> roles);

    DeletionStatus<Void> delete(UserRef user);
}
