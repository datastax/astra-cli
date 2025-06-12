package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.commands.db.AbstractLongRunningDbSpecificCmd;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

public abstract class AbstractLongRunningKeyspaceRequiredCmd extends AbstractLongRunningDbSpecificCmd {
    protected KeyspaceGateway keyspaceGateway;
    protected KeyspaceRef keyspaceRef;

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
        
        keyspaceGateway = KeyspaceGateway.mkDefault(profile().token(), profile().env());
        
        this.keyspaceRef = KeyspaceRef.parse(dbRef, actualKeyspaceRefOption).getRight((msg) -> {
            throw new OptionValidationException("keyspace name", msg);
        });
    }
}
