package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.DbDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.DbDeleteOperation.*;

@Command(
    name = "delete"
)
public class DbDeleteCmd extends AbstractLongRunningDbSpecificCmd<DbDeleteResult> {
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
    protected DbDeleteOperation mkOperation() {
        return new DbDeleteOperation(dbGateway, new DbDeleteRequest(dbRef, ifExists, lrMixin.options()));
    }

    @Override
    protected final OutputAll execute(DbDeleteResult result) {
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
            case DatabaseIllegallyNotFound _ -> {
                throw new DbDeleteOperation.DbNotFoundException(dbRef);
            }
        };
    }
}
