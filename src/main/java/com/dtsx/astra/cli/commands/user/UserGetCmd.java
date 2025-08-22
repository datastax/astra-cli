package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.core.completions.impls.UserEmailsCompletion;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserGetOperation;
import com.dtsx.astra.sdk.org.domain.Role;
import com.dtsx.astra.sdk.org.domain.User;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.user.UserGetOperation.UserGetRequest;

@Command(
    name = "get",
    aliases = { "describe" },
    description = "Show details for a specific user"
)
@Example(
    comment = "Get details for a specific user by email",
    command = "${cli.name} user get john@example.com"
)
@Example(
    comment = "Get details for a specific user by ID",
    command = "${cli.name} user get 12345678-abcd-1234-abcd-123456789012"
)
public class UserGetCmd extends AbstractUserCmd<User> {
    @Parameters(
        description = "User email/id to get",
        paramLabel = "USER",
        completionCandidates = UserEmailsCompletion.class
    )
    public UserRef user;

    @Override
    public OutputJson executeJson(Supplier<User> result) {
        return OutputJson.serializeValue(result);
    }

    @Override
    protected final OutputAll execute(Supplier<User> result) {
        return mkTable(result.get());
    }

    private RenderableShellTable mkTable(User user) {
        val roleNames = user.getRoles().stream()
            .map(Role::getName)
            .toList();

        return ShellTable.forAttributes(new LinkedHashMap<>() {{
            put("User Id", user.getUserId());
            put("User Email", user.getEmail());
            put("Status", user.getStatus().name());
            put("Roles", roleNames);
        }});
    }


    @Override
    protected Operation<User> mkOperation() {
        return new UserGetOperation(userGateway, new UserGetRequest(user));
    }
}
