package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.org.OrgNameOperation;
import picocli.CommandLine.Command;

@Command(name = "name")
public class OrgNameCmd extends AbstractOrgCmd<String> {
    @Override
    public final OutputAll execute(String name) {
        return OutputAll.serializeValue(name);
    }

    @Override
    protected Operation<String> mkOperation() {
        return new OrgNameOperation(orgGateway);
    }
}
