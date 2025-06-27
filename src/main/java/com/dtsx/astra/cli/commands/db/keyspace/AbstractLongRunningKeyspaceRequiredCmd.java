package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.WithSetTimeout;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

public abstract class AbstractLongRunningKeyspaceRequiredCmd<OpRes> extends AbstractKeyspaceCmd<OpRes> implements WithSetTimeout {
    protected KeyspaceRef $keyspaceRef;

    @Mixin
    protected LongRunningOptionsMixin lrMixin;

    @Option(
        names = { "--keyspace", "-k" },
        description = { "The keyspace to use", DEFAULT_VALUE },
        paramLabel = "KEYSPACE",
        required = true
    )
    private String actualKeyspaceRefOption;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        this.$keyspaceRef = KeyspaceRef.parse($dbRef, actualKeyspaceRefOption).getRight((msg) -> {
            throw new OptionValidationException("keyspace name", msg);
        });
    }
}
