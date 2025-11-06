package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.CliConstants.$Collection;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
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
            collectionName = CollectionNamePrompter.prompt(ctx, collectionGateway, $keyspaceRef, collectionPrompt(), (b) -> b.fallbackFlag("-c").fix(originalArgs(), "-c <collection>"));
        }

        this.$collRef = CollectionRef.parse($keyspaceRef, collectionName).getRight((msg) -> {
            throw new OptionValidationException("collection name", msg);
        });
    }

    protected abstract String collectionPrompt();
}
