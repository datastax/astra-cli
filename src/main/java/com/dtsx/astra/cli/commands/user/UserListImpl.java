package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserListOperation;
import com.dtsx.astra.cli.operations.user.UserListOperation.UserInfo;
import lombok.val;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.utils.Collectionutils.sequencedMapOf;

public abstract class UserListImpl extends AbstractUserCmd<Stream<UserInfo>> {
    @Override
    protected final OutputJson executeJson(Supplier<Stream<UserInfo>> result) {
        return OutputJson.serializeValue(result.get().map(UserInfo::raw).toList());
    }

    @Override
    protected final OutputAll execute(Supplier<Stream<UserInfo>> result) {
        val data = result.get()
            .map((user) -> sequencedMapOf(
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
