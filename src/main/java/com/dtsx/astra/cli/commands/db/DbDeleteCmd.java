package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.WithSetTimeout;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.DbDeleteOperation;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dtsx.astra.cli.core.output.ExitCode.DATABASE_NOT_FOUND;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.DbDeleteOperation.*;

@Command(
    name = "delete",
    description = "Delete an existing Astra database"
)
@Example(
    comment = "Delete an existing Astra database",
    command = "astra db delete my_db"
)
@Example(
    comment = "Delete an existing Astra database without failing if it does not exist",
    command = "astra db delete my_db --if-exists"
)
@Example(
    comment = "Delete an existing Astra database without waiting for complete termination",
    command = "astra db delete my_db --async"
)
public class DbDeleteCmd extends AbstractPromptForDbCmd<DbDeleteResult> implements WithSetTimeout {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if database does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean $ifExists;

    @Mixin
    protected LongRunningOptionsMixin lrMixin;

    @Option(names = LR_OPTS_TIMEOUT_NAME, description = LR_OPTS_TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    protected DbDeleteOperation mkOperation() {
        return new DbDeleteOperation(dbGateway, new DbDeleteRequest($dbRef, $ifExists, lrMixin.options()));
    }

    @Override
    protected final OutputAll execute(DbDeleteResult result) {
        return switch (result) {
            case DatabaseNotFound() -> handleDbNotFound($dbRef);
            case DatabaseDeleted() -> handleDbDeleted($dbRef);
            case DatabaseDeletedAndTerminated(var waitTime) -> handleDbDeletedAndTerminated($dbRef, waitTime);
            case DatabaseIllegallyNotFound() -> throwDbNotFound($dbRef);
        };
    }

    private OutputAll handleDbNotFound(DbRef dbRef) {
        val message = "Database %s does not exist; nothing to delete.".formatted(
            highlight(dbRef)
        );

        val data = mkData(false, null);

        return OutputAll.response(message, data, List.of(
            new Hint("See your existing databases:", "astra db list")
        ));
    }

    private OutputAll handleDbDeleted(DbRef dbRef) {
        val message = "Database %s has been deleted (though it may still be terminating, and not yet fully terminated).".formatted(
            highlight(dbRef)
        );

        val data = mkData(true, null);

        return OutputAll.response(message, data, List.of(
            new Hint("Poll its status with:", "astra db status " + dbRef)
        ));
    }

    private OutputAll handleDbDeletedAndTerminated(DbRef dbRef, Duration waitTime) {
        val message = "Database %s has been deleted and fully terminated (waited %ds for termination).".formatted(
            highlight(dbRef),
            waitTime.toSeconds()
        );

        val data = mkData(true, waitTime);

        return OutputAll.response(message, data);
    }

    private <T> T throwDbNotFound(DbRef dbRef) {
        throw new AstraCliException(DATABASE_NOT_FOUND, """
          @|bold,red Error: Database '%s' could not be found.|@
        
          To ignore this error, you can use the @!--if-exists!@ option to avoid failing if the database does not exist.
        """.formatted(
            dbRef
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-exists"),
            new Hint("See your existing databases:", "astra db list")
        ));
    }

    private Map<String, Object> mkData(Boolean wasDeleted, @Nullable Duration waitedDuration) {
        return Map.of(
            "wasDeleted", wasDeleted,
            "waitedSeconds", Optional.ofNullable(waitedDuration).map(Duration::getSeconds)
        );
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to delete";
    }
}
