package com.dtsx.astra.cli.core.output.prompters.specific;

import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import lombok.val;

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


    public static List<String> multiPrompt(CliContext ctx, TableGateway gateway, KeyspaceRef ks, String prompt, List<String> originalArgs) {
        val colls = gateway.findAll(ks).stream().map(TableDescriptor::getName).toList();

        val options = NEList.parse(colls).orElseThrow(() -> new AstraCliException(TABLE_NOT_FOUND, "@|bold,red Error: no tables found to select from|@"));

        return ctx.console().select(prompt)
            .multiOptions(options)
            .requireAnswer()
            .mapper(c -> c)
            .fallbackFlag("-t")
            .fix(originalArgs, "-t <tables>")
            .clearAfterSelection();
    }
}
