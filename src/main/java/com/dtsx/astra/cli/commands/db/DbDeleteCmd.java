package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.DbDeleteOperation;
import com.dtsx.astra.cli.operations.db.DbDeleteOperation.DatabaseDeleted;
import com.dtsx.astra.cli.operations.db.DbDeleteOperation.DatabaseDeletedAndTerminated;
import com.dtsx.astra.cli.operations.db.DbDeleteOperation.DatabaseNotFound;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "delete"
)
public final class DbDeleteCmd extends AbstractLongRunningDbSpecificCmd {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if database does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifExists;

    @Option(names = LR_OPTS_TIMEOUT_NAME, description = LR_OPTS_TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    public OutputAll execute() {
        val result = new DbDeleteOperation(dbGateway).execute(dbRef, ifExists, lrMixin.options());

        return switch (result) {
            case DatabaseNotFound _ -> {
                yield OutputAll.message("Database " + highlight(dbRef) + " does not exist in current org; nothing to delete");
            }
            case DatabaseDeleted _ -> {
                yield OutputAll.message("Database " + highlight(dbRef) + " has been deleted (though it may still be terminating, and not yet fully terminated)");
            }
            case DatabaseDeletedAndTerminated(var waitTime) -> {
                yield OutputAll.message("Database " + highlight(dbRef) + " has been deleted; (waited " + waitTime.toSeconds() + "s for database to be fully terminated)");
            }
        };
    }
}
