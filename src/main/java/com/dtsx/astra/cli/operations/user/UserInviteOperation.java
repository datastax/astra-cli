package com.dtsx.astra.cli.operations.user;

import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserInviteOperation.UserInviteResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserInviteOperation implements Operation<UserInviteResult> {
    private final UserGateway userGateway;
    private final UserInviteRequest request;

    public record UserInviteRequest(UserRef user, String roleId, boolean ifNotExists) {}

    public sealed interface UserInviteResult {}
    public record UserAlreadyExists() implements UserInviteResult {}
    public record UserIllegallyAlreadyExists() implements UserInviteResult {}
    public record UserInvited() implements UserInviteResult {}

    @Override
    public UserInviteResult execute() {
        if (userGateway.tryFindOne(request.user()).isPresent()) {
            return handleUserAlreadyExists(request.ifNotExists());
        }
        
        userGateway.invite(request.user(), request.roleId());
        return new UserInvited();
    }

    private UserInviteResult handleUserAlreadyExists(boolean ifNotExists) {
        if (ifNotExists) {
            return new UserAlreadyExists();
        } else {
            return new UserIllegallyAlreadyExists();
        }
    }
}