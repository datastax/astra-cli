package com.dtsx.astra.cli.commands.role;

import com.dtsx.astra.cli.core.completions.impls.RoleNamesCompletion;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.role.RoleGetOperation;
import com.dtsx.astra.sdk.org.domain.Role;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.role.RoleGetOperation.RoleGetRequest;

@Command(
    name = "get",
    aliases = { "describe" },
    description = "Show details for a specific role"
)
@Example(
    comment = "Get details for a specific role by name",
    command = "${cli.name} role get \"Database Administrator\""
)
@Example(
    comment = "Get details for a specific role by ID",
    command = "${cli.name} role get 12345678-abcd-1234-abcd-123456789012"
)
public class RoleGetCmd extends AbstractRoleCmd<Role> {
    @Parameters(
        description = "Role name/id to get",
        paramLabel = "ROLE",
        completionCandidates = RoleNamesCompletion.class
    )
    public RoleRef role;

    @Override
    protected Operation<Role> mkOperation() {
        return new RoleGetOperation(roleGateway, new RoleGetRequest(role));
    }

    @Override
    public OutputJson executeJson(Supplier<Role> result) {
        return OutputJson.serializeValue(result);
    }

    @Override
    protected final OutputAll execute(Supplier<Role> result) {
        return mkTable(result.get());
    }

    private RenderableShellTable mkTable(Role role) {
        return ShellTable.forAttributes(new LinkedHashMap<>() {{
            put("Identifier", role.getId());
            put("Name", role.getName());
            put("Description", role.getPolicy().getDescription());
            put("Effect", role.getPolicy().getEffect());
            put("Resources", role.getPolicy().getResources());
            put("Actions", role.getPolicy().getActions());
        }});
    }
}
