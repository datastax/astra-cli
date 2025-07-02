package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserListOperation;
import com.dtsx.astra.cli.operations.user.UserListOperation.UserInfo;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class UserListImpl extends AbstractUserCmd<Stream<UserInfo>> {
    @Override
    protected OutputJson executeJson(Stream<UserInfo> result) {
        return OutputJson.serializeValue(result.map(UserInfo::raw).toList());
    }

    @Override
    protected final OutputAll execute(Stream<UserInfo> result) {
        val data = result
            .map((user) -> Map.of(
                "User Id", user.userId(),
                "User Email", user.email(),
                "Status", user.status()
            ))
            .toList();

        return new ShellTable(data).withColumns("User Id", "User Email", "Status");
    }

    @Override
    protected Operation<Stream<UserInfo>> mkOperation() {
        return new UserListOperation(userGateway);
    }
}
