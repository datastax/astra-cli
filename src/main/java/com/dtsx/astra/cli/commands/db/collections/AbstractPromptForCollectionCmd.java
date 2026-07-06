package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.CliConstants.$Collection;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.output.prompters.specific.CollectionNamePrompter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

public abstract class AbstractPromptForCollectionCmd<OpRes> extends AbstractCollectionCmd<OpRes> {
    protected CollectionRef $collRef;

    @Option(
        names = { $Collection.LONG, $Collection.SHORT },
        description = "The collection to use",
        paramLabel = $Collection.LABEL
    )
    private String collectionName;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        if (collectionName == null) {
            collectionName = CollectionNamePrompter.prompt(ctx, collectionGateway, $keyspaceRef, collectionPrompt(), originalArgs());
        }
        this.$collRef = CollectionRef.mustParse($keyspaceRef, collectionName);
    }

    protected abstract String collectionPrompt();
}
