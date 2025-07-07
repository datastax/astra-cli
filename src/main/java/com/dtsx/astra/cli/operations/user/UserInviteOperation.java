package com.dtsx.astra.cli.operations.user;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.CreationStatus.AlreadyExists;
import com.dtsx.astra.cli.core.datatypes.CreationStatus.Created;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserInviteOperation.UserInviteResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class UserInviteOperation implements Operation<UserInviteResult> {
    private final UserGateway userGateway;
    private final UserInviteRequest request;

    public record UserInviteRequest(UserRef user, List<RoleRef> roles, boolean ifNotExists) {}

    public sealed interface UserInviteResult {}
    public record UserAlreadyExists() implements UserInviteResult {}
    public record UserIllegallyAlreadyExists() implements UserInviteResult {}
    public record UserInvited(List<UUID> roleIds) implements UserInviteResult {}

    @Override
    public UserInviteResult execute() {
        val status = userGateway.invite(request.user(), request.roles());

        return switch (status) {
            case CreationStatus.AlreadyExists<?> _ -> handleUserAlreadyExists(request.ifNotExists());
            case CreationStatus.Created<List<UUID>>(var roleIds) -> new UserInvited(roleIds);
        };
    }

    private UserInviteResult handleUserAlreadyExists(boolean ifNotExists) {
        if (ifNotExists) {
            return new UserAlreadyExists();
        } else {
            return new UserIllegallyAlreadyExists();
        }
    }
}
