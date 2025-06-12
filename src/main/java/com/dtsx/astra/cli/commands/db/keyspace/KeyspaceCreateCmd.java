package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceCreateOperation;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceCreateOperation.KeyspaceAlreadyExists;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceCreateOperation.KeyspaceCreated;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceCreateOperation.KeyspaceCreatedAndDbActive;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "create-keyspace"
)
public class KeyspaceCreateCmd extends AbstractLongRunningKeyspaceRequiredCmd {
    @Option(
        names = { "--if-not-exists" },
        description = { "Will create a new keyspace only if none with same name", DEFAULT_VALUE },
        defaultValue = "false"
    )
    protected boolean ifNotExists;

    @Option(names = "--timeout", description = TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public OutputAll execute() {
        val result = new KeyspaceCreateOperation(keyspaceGateway, dbGateway).execute(keyspaceRef, ifNotExists, dontWait, timeout);

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
        };
    }
}
