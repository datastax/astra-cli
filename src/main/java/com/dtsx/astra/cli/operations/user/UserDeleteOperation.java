package com.dtsx.astra.cli.operations.user;

import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserDeleteOperation.UserDeleteResult;
import lombok.RequiredArgsConstructor;

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
        if (userGateway.tryFindOne(request.user()).isEmpty()) {
            return handleUserNotFound(request.ifExists());
        }
        
        userGateway.delete(request.user());
        return new UserDeleted();
    }

    private UserDeleteResult handleUserNotFound(boolean ifExists) {
        if (ifExists) {
            return new UserNotFound();
        } else {
            return new UserIllegallyNotFound();
        }
    }
}