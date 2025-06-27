package com.dtsx.astra.cli.operations.user;

import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserListOperation.UserInfo;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class UserListOperation implements Operation<List<UserInfo>> {
    private final UserGateway userGateway;

    public record UserInfo(
        String userId,
        String email,
        String status
    ) {}

    @Override
    public List<UserInfo> execute() {
        return userGateway.findAll().stream()
            .map(user -> new UserInfo(
                user.getUserId(),
                user.getEmail(),
                user.getStatus().name()
            ))
            .toList();
    }
}