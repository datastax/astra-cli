package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.WithSetTimeout;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.db.DbDeleteOperation;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.ExitCode.DATABASE_NOT_FOUND;
import static com.dtsx.astra.cli.core.output.ExitCode.EXECUTION_CANCELLED;
import static com.dtsx.astra.cli.operations.db.DbDeleteOperation.*;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "delete",
    description = "Delete an existing Astra database"
)
@Example(
    comment = "Delete an existing Astra database",
    command = "${cli.name} db delete my_db"
)
@Example(
    comment = "Delete an existing Astra database without confirmation (required for non-interactive shells)",
    command = "${cli.name} db delete my_db --yes"
)
@Example(
    comment = "Delete an existing Astra database without failing if it does not exist",
    command = "${cli.name} db delete my_db --if-exists"
)
@Example(
    comment = "Delete an existing Astra database without waiting for complete termination",
    command = "${cli.name} db delete my_db --async"
)
public class DbDeleteCmd extends AbstractPromptForDbCmd<DbDeleteResult> implements WithSetTimeout {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if database does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean $ifExists;

    @Option(
        names = { "--yes" },
        description = { "Force deletion of database without prompting", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean $forceDelete;

    @Mixin
    protected LongRunningOptionsMixin lrMixin;

    @Option(names = LR_OPTS_TIMEOUT_NAME, description = LR_OPTS_TIMEOUT_DESC, defaultValue = "800")
    public void setTimeout(int timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    protected final OutputAll execute(Supplier<DbDeleteResult> result) {
        return switch (result.get()) {
            case DatabaseNotFound() -> handleDbNotFound($dbRef);
            case DatabaseDeleted() -> handleDbDeleted($dbRef);
            case DatabaseDeletedAndTerminated(var waitTime) -> handleDbDeletedAndTerminated($dbRef, waitTime);
            case DatabaseIllegallyNotFound() -> throwDbNotFound($dbRef);
        };
    }

    private OutputAll handleDbNotFound(DbRef dbRef) {
        val message = "Database %s does not exist; nothing to delete.".formatted(
            ctx.highlight(dbRef)
        );

        val data = mkData(false, null);

        return OutputAll.response(message, data, List.of(
            new Hint("See your existing databases:", "${cli.name} db list")
        ));
    }

    private OutputAll handleDbDeleted(DbRef dbRef) {
        val message = "Database %s is being deleted (it may not be fully terminated yet).".formatted(
            ctx.highlight(dbRef)
        );

        val data = mkData(true, null);

        return OutputAll.response(message, data, List.of(
            new Hint("Poll its status with:", "${cli.name} db status " + dbRef)
        ));
    }

    private OutputAll handleDbDeletedAndTerminated(DbRef dbRef, Duration waitTime) {
        val message = "Database %s has been deleted and fully terminated (waited %ds for termination).".formatted(
            ctx.highlight(dbRef),
            waitTime.toSeconds()
        );

        val data = mkData(true, waitTime);

        return OutputAll.response(message, data);
    }

    private <T> T throwDbNotFound(DbRef dbRef) {
        throw new AstraCliException(DATABASE_NOT_FOUND, """
          @|bold,red Error: Database '%s' could not be found.|@
        
          To ignore this error, you can use the @'!--if-exists!@ option to avoid failing if the database does not exist.
        """.formatted(
            dbRef
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-exists"),
            new Hint("See your existing databases:", "${cli.name} db list")
        ));
    }

    @Override
    protected DbDeleteOperation mkOperation() {
        return new DbDeleteOperation(dbGateway, new DbDeleteRequest(
            $dbRef,
            $ifExists,
            $forceDelete,
            lrMixin.options(),
            this::assertShouldDelete
        ));
    }

    private void assertShouldDelete(String dbName, UUID id) {
        val prompt = """
          You are about to permanently delete database @!%s!@ @|faint (%s)|@.
        
          To confirm, type the name below or press @!Ctrl+C!@ to cancel.
        """.formatted(dbName, id);

        val shouldDelete = ctx.console().prompt(prompt)
            .mapper(Function.identity())
            .requireAnswer()
            .fallbackFlag("--yes")
            .fix(originalArgs(), "--yes")
            .clearAfterSelection()
            .equals(dbName);

        if (!shouldDelete) {
            throw new AstraCliException(EXECUTION_CANCELLED, """
              @|bold,red Error: User input did not match database name.|@
            
              Database @!%s!@ was not deleted.
            """.formatted(dbName), List.of(
                new Hint("Skip confirmation prompt:", originalArgs(), "--yes")
            ));
        }
    }

    private LinkedHashMap<String, Object> mkData(Boolean wasDeleted, @Nullable Duration waitedDuration) {
        return sequencedMapOf(
            "wasDeleted", wasDeleted,
            "waitedSeconds", Optional.ofNullable(waitedDuration).map(Duration::getSeconds)
        );
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to delete";
    }
}
