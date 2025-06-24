package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.org.OrgIdOperation;
import picocli.CommandLine.Command;

@Command(name = "id")
public class OrgIdCmd extends AbstractOrgCmd<String> {
    @Override
    public final OutputAll execute(String id) {
        return OutputAll.serializeValue(id);
    }

    @Override
    protected Operation<String> mkOperation() {
        return new OrgIdOperation(orgGateway);
    }
}
