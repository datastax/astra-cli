package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceCreateOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.keyspace.KeyspaceCreateOperation.*;

@Command(
    name = "create-keyspace"
)
public class KeyspaceCreateCmd extends AbstractLongRunningKeyspaceRequiredCmd<KeyspaceCreateResult> {
    @Option(
        names = { "--if-not-exists" },
        description = { "Will create a new keyspace only if none with same name", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifNotExists;

    @Option(names = LR_OPTS_TIMEOUT_NAME, description = LR_OPTS_TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    protected KeyspaceCreateOperation mkOperation() {
        return new KeyspaceCreateOperation(keyspaceGateway, dbGateway, new KeyspaceCreateRequest(keyspaceRef, ifNotExists, lrMixin.options()));
    }

    @Override
    protected final OutputAll execute(KeyspaceCreateResult result) {
        return switch (result) {
            case KeyspaceAlreadyExists _ -> {
                yield OutputAll.message("Keyspace " + highlight(keyspaceRef) + " already exists in database " + highlight(keyspaceRef.db()));
            }
            case KeyspaceCreated _ -> {
                yield OutputAll.message("Keyspace " + highlight(keyspaceRef) + " has been created in database " + highlight(keyspaceRef.db()) + " (database may not be active yet)");
            }
            case KeyspaceCreatedAndDbActive(var waitTime) -> {
                yield OutputAll.message("Keyspace " + highlight(keyspaceRef) + " has been created in database " + highlight(keyspaceRef.db()) + 
                    " (waited " + waitTime.toSeconds() + "s for database to become active)");
            }
            case KeyspaceIllegallyAlreadyExists _ -> {
                throw new KeyspaceCreateOperation.KeyspaceAlreadyExistsException(keyspaceRef);
            }
        };
    }
}
