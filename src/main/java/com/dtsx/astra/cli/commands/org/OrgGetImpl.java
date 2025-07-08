package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.org.OrgGetOperation;
import com.dtsx.astra.sdk.org.domain.Organization;

import java.util.LinkedHashMap;

public abstract class OrgGetImpl extends AbstractOrgCmd<Organization> {
    @Override
    protected OutputJson executeJson(Organization org) {
        return OutputJson.serializeValue(org);
    }

    @Override
    public final OutputAll execute(Organization organization) {
        return ShellTable.forAttributes(new LinkedHashMap<>() {{
            put("Name", organization.getName());
            put("Id", organization.getId());
        }});
    }

    @Override
    protected Operation<Organization> mkOperation() {
        return new OrgGetOperation(orgGateway);
    }
}
