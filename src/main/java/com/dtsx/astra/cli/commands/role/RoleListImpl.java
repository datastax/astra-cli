package com.dtsx.astra.cli.commands.role;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.role.RoleListOperation;
import com.dtsx.astra.cli.operations.role.RoleListOperation.RoleInfo;
import lombok.val;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class RoleListImpl extends AbstractRoleCmd<Stream<RoleInfo>> {
    @Override
    protected OutputJson executeJson(Supplier<Stream<RoleInfo>> result) {
        return OutputJson.serializeValue(result.get().map(RoleInfo::raw).toList());
    }

    @Override
    protected final OutputAll execute(Supplier<Stream<RoleInfo>> result) {
        val data = result.get()
            .map((role) -> Map.of(
                "Role Id", role.id(),
                "Role Name", role.name()
            ))
            .toList();

        return new ShellTable(data).withColumns("Role Id", "Role Name");
    }

    @Override
    protected Operation<Stream<RoleInfo>> mkOperation() {
        return new RoleListOperation(roleGateway);
    }
}
