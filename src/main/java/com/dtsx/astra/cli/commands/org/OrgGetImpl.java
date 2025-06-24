package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.org.OrgGetOperation;
import com.dtsx.astra.sdk.org.domain.Organization;

import java.util.List;

public abstract class OrgGetImpl extends AbstractOrgCmd<Organization> {
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
