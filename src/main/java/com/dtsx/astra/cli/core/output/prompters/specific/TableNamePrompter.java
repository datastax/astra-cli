package com.dtsx.astra.cli.core.output.prompters.specific;

import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;

import java.util.List;

import static com.dtsx.astra.cli.core.output.ExitCode.TABLE_NOT_FOUND;

public class TableNamePrompter {
    public static String prompt(CliContext ctx, TableGateway gateway, KeyspaceRef ks, String prompt, List<String> originalArgs) {
        return SpecificPrompter.<TableDescriptor, String>run(ctx, (b) -> b
            .thing("table")
            .prompt(prompt)
            .thingNotFoundCode(TABLE_NOT_FOUND)
            .thingsSupplier(() -> gateway.findAll(ks))
            .getThingIdentifier(TableDescriptor::getName)
            .fix(f -> f.fallbackFlag("-t").fix(originalArgs, "-t <table>"))
            .mapSingleFound(TableDescriptor::getName)
        );
    }
}
