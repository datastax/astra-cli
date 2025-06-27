package com.dtsx.astra.cli.commands.role;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.role.RoleListOperation;
import com.dtsx.astra.cli.operations.role.RoleListOperation.RoleInfo;
import lombok.val;

import java.util.List;
import java.util.Map;

public abstract class RoleListImpl extends AbstractRoleCmd<List<RoleInfo>> {
    @Override
    protected Operation<List<RoleInfo>> mkOperation() {
        return new RoleListOperation(roleGateway);
    }

    @Override
    protected final OutputAll execute(List<RoleInfo> result) {
        val data = result.stream()
            .map((role) -> Map.of(
                "Role Id", role.id(),
                "Role Name", role.name(),
                "Description", role.description()
            ))
            .toList();

        return new ShellTable(data).withColumns("Role Id", "Role Name", "Description");
    }
}
