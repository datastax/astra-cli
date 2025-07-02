package com.dtsx.astra.cli.operations.user;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserDeleteOperation.UserDeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class UserDeleteOperation implements Operation<UserDeleteResult> {
    private final UserGateway userGateway;
    private final UserDeleteRequest request;

    public record UserDeleteRequest(UserRef user, boolean ifExists) {}

    public sealed interface UserDeleteResult {}
    public record UserNotFound() implements UserDeleteResult {}
    public record UserIllegallyNotFound() implements UserDeleteResult {}
    public record UserDeleted() implements UserDeleteResult {}

    @Override
    public UserDeleteResult execute() {
        val status = userGateway.delete(request.user());

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> new UserDeleted();
            case DeletionStatus.NotFound<?> _ -> handleUserNotFound(request.ifExists());
        };
    }

    private UserDeleteResult handleUserNotFound(boolean ifExists) {
        if (ifExists) {
            return new UserNotFound();
        } else {
            return new UserIllegallyNotFound();
        }
    }
}