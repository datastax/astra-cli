package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceDeleteOperation;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceDeleteOperation.KeyspaceDeleted;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceDeleteOperation.KeyspaceNotFound;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceDeleteOperation.KeyspaceDeletedAndDbActive;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "delete-keyspace"
)
public final class KeyspaceDeleteCmd extends AbstractLongRunningKeyspaceRequiredCmd {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if keyspace does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifExists;

    @Option(names = LR_OPTS_TIMEOUT_NAME, description = LR_OPTS_TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    public OutputAll execute() {
        val result = new KeyspaceDeleteOperation(keyspaceGateway, dbGateway).execute(keyspaceRef, ifExists, lrMixin.options());

        return switch (result) {
            case KeyspaceNotFound _ -> {
                yield OutputAll.message("Keyspace " + highlight(keyspaceRef) + " does not exist in database " + highlight(keyspaceRef.db()) + "; nothing to delete");
            }
            case KeyspaceDeleted _ -> {
                yield OutputAll.message("Keyspace " + highlight(keyspaceRef) + " has been deleted from database " + highlight(keyspaceRef.db()) + " (database may not be active yet)");
            }
            case KeyspaceDeletedAndDbActive(var waitTime) -> {
                yield OutputAll.message("Keyspace " + highlight(keyspaceRef) + " has been deleted from database " + highlight(keyspaceRef.db()) + " (waited " + waitTime.toSeconds() + "s for database to become active)");
            }
        };
    }
}
