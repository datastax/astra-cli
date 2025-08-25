package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.CliConstants.$Collection;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.models.CollectionRef;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

public abstract class AbstractCollectionSpecificCmd<OpRes> extends AbstractCollectionCmd<OpRes> {
    protected CollectionRef $collRef;

    @Option(
        names = { $Collection.LONG, $Collection.SHORT },
        description = "The collection to use",
        paramLabel = $Collection.LABEL,
        required = true
    )
    private String actualCollectionRefOption;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        this.$collRef = CollectionRef.parse($keyspaceRef, actualCollectionRefOption).getRight((msg) -> {
            throw new OptionValidationException("collection name", msg);
        });
    }
}
