package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.DbResumeOperation;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.DbResumeOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "resume",
    description = "Resume a database that is in a stopped state. If the database is already active, no action is taken."
)
@Example(
    comment = "Resume a database",
    command = "astra db resume my_db"
)
@Example(
    comment = "Resume a database, without waiting for it to become active",
    command = "astra db resume my_db --async"
)
public class DbResumeCmd extends AbstractLongRunningDbSpecificCmd<DbResumeResult> {
    @Option(names = LR_OPTS_TIMEOUT_NAME, description = LR_OPTS_TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    protected DbResumeOperation mkOperation() {
        return new DbResumeOperation(dbGateway, new DbResumeRequest($dbRef, lrMixin.options()));
    }

    @Override
    protected final OutputAll execute(DbResumeResult result) {
        val message = switch (result) {
            case DatabaseAwaited(var duration) -> handleDatabaseAwaited(duration);
            case DatabaseResumedAwaited(var duration) -> handleDatabaseResumedAwaited(duration);
            case DatabaseNeedsAwaiting(var database) -> handleDatabaseNeedsAwaiting(database);
            case DatabaseResumedNeedsAwaiting(var database) -> handleDatabaseResumedNeedsAwaiting(database);
            case DatabaseAlreadyActiveNoWait() -> handleDatabaseAlreadyActiveNoWait();
        };

        return OutputAll.message(trimIndent(message));
    }

    private String handleDatabaseAwaited(Duration duration) {
        return """
          Database %s was not in a state that required resuming, but is now active after waiting %d seconds.
        """.formatted(
            highlight($dbRef),
            duration.getSeconds()
        );
    }

    private String handleDatabaseResumedAwaited(Duration duration) {
        return """
          Database %s was resumed, and is now active after waiting %d seconds.
        """.formatted(
            highlight($dbRef),
            duration.getSeconds()
        );
    }

    private String handleDatabaseNeedsAwaiting(Database database) {
        val currentStatus = database.getStatus();

        return """
          Database %s was not in a state that required resuming, but is not yet active (current status: %s).
        
          %s
          %s
        """.formatted(
            highlight($dbRef),
            highlight(currentStatus),
            renderComment("Poll the database's status:"),
            renderCommand("astra db status %s".formatted($dbRef))
        );
    }

    private String handleDatabaseResumedNeedsAwaiting(Database database) {
        val currentStatus = database.getStatus();

        return """
          Database %s was resumed, but is not yet active (current status: %s).
        
          %s
          %s
        """.formatted(
            highlight($dbRef),
            highlight(currentStatus),
            renderComment("Poll the database's status:"),
            renderCommand("astra db status %s".formatted($dbRef))
        );
    }

    private String handleDatabaseAlreadyActiveNoWait() {
        return """
          Database %s is already active; no action was required.
        """.formatted(highlight($dbRef));
    }
}
