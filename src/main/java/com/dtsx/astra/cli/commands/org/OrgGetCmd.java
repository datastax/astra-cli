package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.org.OrgGetOperation;
import com.dtsx.astra.sdk.org.domain.Organization;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.List;

@Command(
    name = "get"
)
public class OrgGetCmd extends AbstractOrgCmd<Organization> {
    @Override
    public final OutputAll execute(Organization organization) {
        return new ShellTable(List.of(
            ShellTable.attr("Name", organization.getName()),
            ShellTable.attr("id", organization.getId())
        )).withAttributeColumns();
    }

    @Override
    protected Operation<Organization> mkOperation() {
        return new OrgGetOperation(orgGateway);
    }
}
