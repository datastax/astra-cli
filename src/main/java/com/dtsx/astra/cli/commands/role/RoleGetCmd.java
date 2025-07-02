package com.dtsx.astra.cli.commands.role;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.role.RoleGetOperation;
import com.dtsx.astra.sdk.org.domain.Role;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

import static com.dtsx.astra.cli.operations.role.RoleGetOperation.RoleGetRequest;

@Command(
    name = "get",
    aliases = { "describe" }
)
public class RoleGetCmd extends AbstractRoleCmd<Role> {
    @Parameters(index = "0", paramLabel = "ROLE")
    public String role;

    @Override
    protected Operation<Role> mkOperation() {
        return new RoleGetOperation(roleGateway, new RoleGetRequest(role));
    }

    @Override
    public OutputJson executeJson(Role result) {
        return OutputJson.serializeValue(result);
    }

    @Override
    protected final OutputAll execute(Role result) {
        return mkTable(result);
    }

    private RenderableShellTable mkTable(Role role) {
        return new ShellTable(List.of(
            ShellTable.attr("Identifier", role.getId()),
            ShellTable.attr("Name", role.getName()),
            ShellTable.attr("Description", role.getPolicy().getDescription()),
            ShellTable.attr("Effect", role.getPolicy().getEffect()),
            ShellTable.attr("Resources", role.getPolicy().getResources()),
            ShellTable.attr("Actions", role.getPolicy().getActions())
        )).withAttributeColumns();
    }
}
