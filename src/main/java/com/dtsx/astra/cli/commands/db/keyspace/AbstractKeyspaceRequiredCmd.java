package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.CliConstants.$Keyspace;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

public abstract class AbstractKeyspaceRequiredCmd<OpRes> extends AbstractKeyspaceCmd<OpRes> {
    protected KeyspaceRef $keyspaceRef;

    @Option(
        names = { $Keyspace.LONG, $Keyspace.SHORT },
        description = "The keyspace to use",
        paramLabel = $Keyspace.LABEL,
        required = true
    )
    private String $keyspaceName;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        this.$keyspaceRef = KeyspaceRef.mustParse($dbRef, $keyspaceName);
    }
}
