package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.DbDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.DbDeleteOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

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
public class DbDeleteCmd extends AbstractLongRunningDbSpecificCmd<DbDeleteResult> {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if database does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean $ifExists;

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
        val message = switch (result) {
            case DatabaseNotFound() -> handleDbNotFound($dbRef);
            case DatabaseDeleted() -> handleDbDeleted($dbRef);
            case DatabaseDeletedAndTerminated(var waitTime) -> handleDbDeletedAndTerminated($dbRef, waitTime);
            case DatabaseIllegallyNotFound() -> throwDbNotFound($dbRef);
        };

        return OutputAll.message(trimIndent(message));
    }

    private String handleDbNotFound(DbRef dbRef) {
        return """
          Database %s does not exist; nothing to delete.

          %s
          %s
        """.formatted(
            highlight(dbRef),
            renderComment("See your existing databases:"),
            renderCommand("astra db list")
        );
    }

    private String handleDbDeleted(DbRef dbRef) {
        return """
          Database %s has been deleted (though it may still be terminating, and not yet fully terminated).
        
          %s
          %s
        """.formatted(
            highlight(dbRef),
            renderComment("Poll its status with:"),
            renderCommand("astra db status " + dbRef)
        );
    }

    private String handleDbDeletedAndTerminated(DbRef dbRef, Duration waitTime) {
        return """
          Database %s has been deleted and fully terminated (waited %ds for termination).
        """.formatted(
            highlight(dbRef),
            waitTime.toSeconds()
        );
    }

    private String throwDbNotFound(DbRef dbRef) {
        throw new AstraCliException("""
          @|bold,red Error: Database '%s' could not be found.|@
        
          To ignore this error, you can use the %s option to avoid failing if the profile does not exist.
        
          %s
          %s
        
          %s
          %s
        """.formatted(
            dbRef,
            AstraColors.highlight("--if-exists"),
            renderComment("Example fix:"),
            renderCommand(originalArgs(), "--if-exists"),
            renderComment("See your existing databases:"),
            renderCommand("astra db list")
        ));
    }
}
