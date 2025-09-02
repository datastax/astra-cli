package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.org.OrgGetOperation;
import com.dtsx.astra.sdk.org.domain.Organization;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public abstract class OrgGetImpl extends AbstractOrgCmd<Organization> {
    @Override
    protected final OutputJson executeJson(Supplier<Organization> org) {
        return OutputJson.serializeValue(org.get());
    }

    @Override
    public final OutputAll execute(Supplier<Organization> organization) {
        return ShellTable.forAttributes(new LinkedHashMap<>() {{
            put("Name", organization.get().getName());
            put("Id", organization.get().getId());
        }});
    }

    @Override
    protected Operation<Organization> mkOperation() {
        return new OrgGetOperation(orgGateway);
    }
}
