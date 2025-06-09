package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.domain.db.collections.CollectionRef;
import com.dtsx.astra.cli.exceptions.cli.OptionValidationException;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

public class AbstractCollectionSpecificCmd extends AbstractCollectionCmd {
    protected CollectionRef collRef;

    @Option(
        names = { "--collection", "-c" },
        description = { "The collection to use", DEFAULT_VALUE },
        paramLabel = "COLLECTION",
        required = true
    )
    private String actualCollectionRefOption;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        this.collRef = CollectionRef.parse(keyspaceRef, actualCollectionRefOption).getRight((msg) -> {
            throw new OptionValidationException("collection name", msg);
        });
    }
}
