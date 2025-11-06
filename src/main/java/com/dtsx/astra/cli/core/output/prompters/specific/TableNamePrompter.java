package com.dtsx.astra.cli.core.output.prompters.specific;

import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsClearAfterSelection;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsFallback;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;

import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.ExitCode.TABLE_NOT_FOUND;

public class TableNamePrompter {
    public static String prompt(CliContext ctx, TableGateway gateway, KeyspaceRef ks, String prompt, Function<NeedsFallback<TableDescriptor>, NeedsClearAfterSelection<TableDescriptor>> fix) {
        return SpecificPrompter.<TableDescriptor, String>run(ctx, (b) -> b
            .thing("table")
            .prompt(prompt)
            .thingNotFoundCode(TABLE_NOT_FOUND)
            .thingsSupplier(() -> gateway.findAll(ks))
            .getThingIdentifier(TableDescriptor::getName)
            .fix(fix)
            .mapSingleFound(TableDescriptor::getName)
        );
    }
}
