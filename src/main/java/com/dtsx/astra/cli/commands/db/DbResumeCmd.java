package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.DbResumeOperation;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.DbResumeOperation.*;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;

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
        return switch (result) {
            case DatabaseAwaited(var duration) -> handleDatabaseAwaited(duration);
            case DatabaseResumedAwaited(var duration) -> handleDatabaseResumedAwaited(duration);
            case DatabaseNeedsAwaiting(var database) -> handleDatabaseNeedsAwaiting(database);
            case DatabaseResumedNeedsAwaiting(var database) -> handleDatabaseResumedNeedsAwaiting(database);
            case DatabaseAlreadyActiveNoWait() -> handleDatabaseAlreadyActiveNoWait();
        };
    }

    private OutputAll handleDatabaseAwaited(Duration duration) {
        val message = "Database %s was not in a state that required resuming, but is now active after waiting %d seconds.".formatted(
            highlight($dbRef),
            duration.getSeconds()
        );

        val data = mkData(false, ACTIVE, duration);

        return OutputAll.response(message, data);
    }

    private OutputAll handleDatabaseResumedAwaited(Duration duration) {
        val message = "Database %s was resumed, and is now active after waiting %d seconds.".formatted(
            highlight($dbRef),
            duration.getSeconds()
        );

        val data = mkData(true, ACTIVE, duration);

        return OutputAll.response(message, data);
    }

    private OutputAll handleDatabaseNeedsAwaiting(Database database) {
        val currentStatus = database.getStatus();
        val message = "Database %s was not in a state that required resuming, but is not yet active (current status: %s).".formatted(
            highlight($dbRef),
            highlight(currentStatus)
        );

        val data = mkData(false, currentStatus, null);

        return OutputAll.response(message, data, List.of(
            new Hint("Poll the database's status:", "astra db status %s".formatted($dbRef))
        ));
    }

    private OutputAll handleDatabaseResumedNeedsAwaiting(Database database) {
        val currentStatus = database.getStatus();

        val message = "Database %s was resumed, but is not yet active (current status: %s).".formatted(
            highlight($dbRef),
            highlight(currentStatus)
        );

        val data = mkData(true, currentStatus, null);

        return OutputAll.response(message, data, List.of(
            new Hint("Poll the database's status:", "astra db status %s".formatted($dbRef))
        ));
    }

    private OutputAll handleDatabaseAlreadyActiveNoWait() {
        val message = "Database %s is already active; no action was required.".formatted(highlight($dbRef));

        val data = mkData(false, ACTIVE, Duration.ZERO);

        return OutputAll.response(message, data);
    }

    private Map<String, Object> mkData(Boolean wasResumed, DatabaseStatusType currentStatus, @Nullable Duration waitedDuration) {
        return Map.of(
            "wasHibernated", wasResumed,
            "currentStatus", currentStatus,
            "waitedSeconds", Optional.ofNullable(waitedDuration).map(Duration::getSeconds)
        );
    }
}
