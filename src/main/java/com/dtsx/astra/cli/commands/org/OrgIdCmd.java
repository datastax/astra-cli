package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.org.OrgIdOperation;
import lombok.val;
import picocli.CommandLine.Command;

@Command(name = "id")
public final class OrgIdCmd extends AbstractOrgCmd {
    @Override
    public OutputAll execute() {
        val operation = new OrgIdOperation(orgGateway);
        val id = operation.execute();
        return OutputAll.serializeValue(id);
    }
}
