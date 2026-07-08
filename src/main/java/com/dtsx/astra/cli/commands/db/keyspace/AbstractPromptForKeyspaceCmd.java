package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.CliConstants.$Keyspace;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.output.prompters.specific.KeyspacePrompter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

public abstract class AbstractPromptForKeyspaceCmd<OpRes> extends AbstractKeyspaceCmd<OpRes> {
    protected KeyspaceRef $keyspaceRef;

    @Option(
        names = { $Keyspace.LONG, $Keyspace.SHORT },
        description = "The keyspace to use",
        paramLabel = $Keyspace.LABEL
    )
    private String keyspaceName;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        if (!parseKeyspaceRef()) {
            return;
        }

        if (keyspaceName == null) {
            if (shouldFindDefaultKeyspace()) {
                this.$keyspaceRef = dbGateway.tryFindDefaultKeyspace($dbRef).orElse(null); // it'll be cached most of the time anyway
            }

            if (this.$keyspaceRef == null) {
                this.$keyspaceRef = KeyspacePrompter.prompt(ctx, keyspaceGateway, $dbRef, keyspacePrompt(), (b) -> b.fallbackFlag("-k").fix(originalArgs(), "-k <keyspace>"));
            }
        }

        if (this.$keyspaceRef == null) {
            this.$keyspaceRef = KeyspaceRef.mustParse($dbRef, keyspaceName);
        }
    }

    protected boolean shouldFindDefaultKeyspace() {
        return true;
    }

    protected boolean parseKeyspaceRef() {
        return true;
    }

    protected String keyspacePrompt() {
        return "Select the keyspace to use:";
    }
}
