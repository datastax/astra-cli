package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.org.OrgIdOperation;
import picocli.CommandLine.Command;

import java.util.function.Supplier;

@Command(
    name = "id",
    description = "Show organization ID"
)
@Example(
    comment = "Get your organization ID",
    command = "astra org id"
)
public class OrgIdCmd extends AbstractOrgCmd<String> {
    @Override
    public final OutputAll execute(Supplier<String> id) {
        return OutputAll.serializeValue(id.get());
    }

    @Override
    protected Operation<String> mkOperation() {
        return new OrgIdOperation(orgGateway);
    }
}
