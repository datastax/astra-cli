package com.dtsx.astra.cli.core.output.prompters.specific;

import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;

import java.util.List;

import static com.dtsx.astra.cli.core.output.ExitCode.COLLECTION_NOT_FOUND;

public class CollectionNamePrompter {
    public static String prompt(CliContext ctx, CollectionGateway gateway, KeyspaceRef ks, String prompt, List<String> originalArgs) {
        return SpecificPrompter.<CollectionDescriptor, String>run(ctx, (b) -> b
            .thing("collection")
            .prompt(prompt)
            .thingNotFoundCode(COLLECTION_NOT_FOUND)
            .thingsSupplier(() -> gateway.findAll(ks))
            .getThingIdentifier(CollectionDescriptor::getName)
            .fix(f -> f.fallbackFlag("-c").fix(originalArgs, "-c <collection>"))
            .mapSingleFound(CollectionDescriptor::getName)
        );
    }
}
