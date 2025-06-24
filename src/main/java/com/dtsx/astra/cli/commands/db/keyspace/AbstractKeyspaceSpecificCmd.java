package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

public abstract class AbstractKeyspaceSpecificCmd<OpRes> extends AbstractKeyspaceCmd<OpRes> {
    protected KeyspaceRef keyspaceRef;

    @Option(
        names = { "--keyspace", "-k" },
        description = { "The keyspace to use", DEFAULT_VALUE },
        paramLabel = "KEYSPACE",
        defaultValue = "default_keyspace"
    )
    private String actualKeyspaceRefOption;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        this.keyspaceRef = KeyspaceRef.parse(dbRef, actualKeyspaceRefOption).getRight((msg) -> {
            throw new OptionValidationException("keyspace name", msg);
        });
    }
}
