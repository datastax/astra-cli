package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.org.OrgGetOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.List;

@Command(
    name = "get"
)
public final class OrgGetCmd extends AbstractOrgCmd {
    @Override
    public OutputAll execute() {
        val operation = new OrgGetOperation(orgGateway);
        val organization = operation.execute();
        
        return new ShellTable(List.of(
            ShellTable.attr("Name", organization.getName()),
            ShellTable.attr("id", organization.getId())
        )).withAttributeColumns();
    }
}
