package com.dtsx.astra.cli.core.output.prompters.specific;

import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import lombok.val;

import java.util.List;

import static com.dtsx.astra.cli.core.output.ExitCode.COLLECTION_NOT_FOUND;

public class CollectionNamePrompter {
    public static String prompt(CliContext ctx, CollectionGateway gateway, KeyspaceRef ks, String prompt, List<String> originalArgs) {
        return SpecificPrompter.<CollectionDescriptor, String>run(ctx, (b) -> b
            .thing("collection")
            .prompt(prompt)
            .thingNotFoundCode(COLLECTION_NOT_FOUND)
            .thingsSupplier(() -> gateway.findAll(ks, true))
            .getThingIdentifier(CollectionDescriptor::getName)
            .fix(f -> f.fallbackFlag("-c").fix(originalArgs, "-c <collection>"))
            .mapSingleFound(CollectionDescriptor::getName)
        );
    }

    public static List<String> multiPrompt(CliContext ctx, CollectionGateway gateway, KeyspaceRef ks, String prompt, List<String> originalArgs) {
        val colls = gateway.findAll(ks, true).stream().map(CollectionDescriptor::getName).toList();

        val options = NEList.parse(colls).orElseThrow(() -> new AstraCliException(COLLECTION_NOT_FOUND, "@|bold,red Error: no collections found to select from|@"));

        return ctx.console().select(prompt)
            .multiOptions(options)
            .requireAnswer()
            .mapper(c -> c)
            .fallbackFlag("-c")
            .fix(originalArgs, "-c <collections>")
            .clearAfterSelection();
    }
}
