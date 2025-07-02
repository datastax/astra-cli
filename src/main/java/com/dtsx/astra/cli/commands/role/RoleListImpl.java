package com.dtsx.astra.cli.commands.role;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.role.RoleListOperation;
import com.dtsx.astra.cli.operations.role.RoleListOperation.RoleInfo;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class RoleListImpl extends AbstractRoleCmd<Stream<RoleInfo>> {
    @Override
    protected OutputJson executeJson(Stream<RoleInfo> result) {
        return OutputJson.serializeValue(result.map(RoleInfo::raw).toList());
    }

    @Override
    protected final OutputAll execute(Stream<RoleInfo> result) {
        val data = result
            .map((role) -> Map.of(
                "Role Id", role.id(),
                "Role Name", role.name(),
                "Description", role.description()
            ))
            .toList();

        return new ShellTable(data).withColumns("Role Id", "Role Name", "Description");
    }

    @Override
    protected Operation<Stream<RoleInfo>> mkOperation() {
        return new RoleListOperation(roleGateway);
    }
}
