package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.org.OrgNameOperation;
import picocli.CommandLine.Command;

import java.util.function.Supplier;

@Command(
    name = "name",
    description = "Get your organization's name"
)
@Example(
    comment = "Get your organization's name",
    command = "${cli.name} org name"
)
public class OrgNameCmd extends AbstractOrgCmd<String> {
    @Override
    public final OutputAll execute(Supplier<String> name) {
        return OutputAll.serializeValue(name.get());
    }

    @Override
    protected Operation<String> mkOperation() {
        return new OrgNameOperation(orgGateway);
    }
}
