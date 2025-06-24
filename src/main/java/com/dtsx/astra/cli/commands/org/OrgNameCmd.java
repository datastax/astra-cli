package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.org.OrgNameOperation;
import lombok.val;
import picocli.CommandLine.Command;

@Command(name = "name")
public final class OrgNameCmd extends AbstractOrgCmd {
    @Override
    public OutputAll execute() {
        val operation = new OrgNameOperation(orgGateway);
        val name = operation.execute();
        return OutputAll.serializeValue(name);
    }
}
