package com.dtsx.astra.cli.operations.user;

import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserListOperation.UserInfo;
import com.dtsx.astra.sdk.org.domain.User;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class UserListOperation implements Operation<Stream<UserInfo>> {
    private final UserGateway userGateway;

    public record UserInfo(
        String userId,
        String email,
        String status,
        User raw
    ) {}

    @Override
    public Stream<UserInfo> execute() {
        return userGateway.findAll()
            .map(user -> new UserInfo(
                user.getUserId(),
                user.getEmail(),
                user.getStatus().name(),
                user
            ));
    }
}
