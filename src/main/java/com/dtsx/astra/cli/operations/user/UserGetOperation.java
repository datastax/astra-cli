package com.dtsx.astra.cli.operations.user;

import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.org.domain.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserGetOperation implements Operation<User> {
    private final UserGateway userGateway;
    private final UserGetRequest request;

    public record UserGetRequest(UserRef user) {}

    @Override
    public User execute() {
        return userGateway.findOne(request.user());
    }
}
