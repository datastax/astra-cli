package com.dtsx.astra.cli.core.exceptions.internal.user;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.UserRef;

import static com.dtsx.astra.cli.core.output.ExitCode.USER_NOT_FOUND;

public class UserNotFoundException extends AstraCliException {
    public UserNotFoundException(UserRef role) {
        super(USER_NOT_FOUND, """
          @|bold,red Error: User '%s' not found.|@
        
          Use @!${cli.name} user list!@ to see all available users.
        """.formatted(role));
    }
}
