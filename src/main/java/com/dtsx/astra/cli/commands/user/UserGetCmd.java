package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserGetOperation;
import com.dtsx.astra.sdk.org.domain.Role;
import com.dtsx.astra.sdk.org.domain.User;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

import static com.dtsx.astra.cli.operations.user.UserGetOperation.UserGetRequest;

@Command(
    name = "get",
    aliases = { "describe" },
    description = "Show user details"
)
public class UserGetCmd extends AbstractUserCmd<User> {
    @Parameters(description = "User email or identifier")
    public UserRef user;

    @Override
    public OutputJson executeJson(User result) {
        return OutputJson.serializeValue(result);
    }

    @Override
    protected final OutputAll execute(User result) {
        return mkTable(result);
    }

    private RenderableShellTable mkTable(User user) {
        val roleNames = user.getRoles().stream()
            .map(Role::getName)
            .toList();

        return new ShellTable(List.of(
            ShellTable.attr("User Id", user.getUserId()),
            ShellTable.attr("User Email", user.getEmail()),
            ShellTable.attr("Status", user.getStatus().name()),
            ShellTable.attr("Roles", roleNames)
        )).withAttributeColumns();
    }

    @Override
    protected Operation<User> mkOperation() {
        return new UserGetOperation(userGateway, new UserGetRequest(user));
    }
}
