package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserListOperation;
import com.dtsx.astra.cli.operations.user.UserListOperation.UserInfo;
import lombok.val;

import java.util.List;
import java.util.Map;

public abstract class UserListImpl extends AbstractUserCmd<List<UserInfo>> {
    @Override
    protected Operation<List<UserInfo>> mkOperation() {
        return new UserListOperation(userGateway);
    }

    @Override
    protected final OutputAll execute(List<UserInfo> result) {
        val data = result.stream()
            .map((user) -> Map.of(
                "User Id", user.userId(),
                "User Email", user.email(),
                "Status", user.status()
            ))
            .toList();

        return new ShellTable(data).withColumns("User Id", "User Email", "Status");
    }
}
