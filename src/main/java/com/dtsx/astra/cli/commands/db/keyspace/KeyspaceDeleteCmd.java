package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceDeleteOperation;
import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.KEYSPACE_NOT_FOUND;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.keyspace.KeyspaceDeleteOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "delete-keyspace",
    description = "Delete a keyspace from the specified database"
)
@Example(
    comment = "Delete a keyspace",
    command = "astra db delete-keyspace my_db -k my_keyspace"
)
@Example(
    comment = "Delete a keyspace without failing if it doesn't exist",
    command = "astra db delete-keyspace my_db -k my_keyspace --if-exists"
)
@Example(
    comment = "Delete a keyspace without waiting for the database to become active",
    command = "astra db delete-keyspace my_db -k my_keyspace --async"
)
public class KeyspaceDeleteCmd extends AbstractLongRunningKeyspaceRequiredCmd<KeyspaceDeleteResult> {
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
    protected KeyspaceDeleteOperation mkOperation() {
        return new KeyspaceDeleteOperation(keyspaceGateway, dbGateway, new KeyspaceDeleteRequest($keyspaceRef, ifExists, lrMixin.options()));
    }

    @Override
    protected final OutputAll execute(KeyspaceDeleteResult result) {
        return switch (result) {
            case KeyspaceNotFound() -> handleKeyspaceNotFound();
            case KeyspaceDeleted() -> handleKeyspaceDeleted();
            case KeyspaceDeletedAndDbActive(var waitTime) -> handleKeyspaceDeletedAndDbActive(waitTime);
            case KeyspaceIllegallyNotFound() -> throwKeyspaceNotFound();
        };
    }

    private OutputAll handleKeyspaceNotFound() {
        val message = "Keyspace %s does not exist in database %s; nothing to delete.".formatted(
            highlight($keyspaceRef.name()),
            highlight($keyspaceRef.db())
        );

        val data = mkData(false, null);

        return OutputAll.response(message, data, List.of(
            new Hint("List existing keyspaces:", "astra db list-keyspaces %s".formatted($keyspaceRef.db()))
        ));
    }

    private OutputAll handleKeyspaceDeleted() {
        val message = "Keyspace %s has been deleted from database %s (database may not be active yet).".formatted(
            highlight($keyspaceRef.name()),
            highlight($keyspaceRef.db())
        );

        val data = mkData(true, null);

        return OutputAll.response(message, data, List.of(
            new Hint("Check the database status:", "astra db status %s".formatted($keyspaceRef.db()))
        ));
    }

    private OutputAll handleKeyspaceDeletedAndDbActive(Duration waitTime) {
        val message = "Keyspace %s has been deleted from database %s. The database is now active after waiting %d seconds.".formatted(
            highlight($keyspaceRef.name()),
            highlight($keyspaceRef.db()),
            waitTime.toSeconds()
        );

        val data = mkData(true, waitTime);

        return OutputAll.response(message, data);
    }

    private <T> T throwKeyspaceNotFound() {
        throw new AstraCliException(KEYSPACE_NOT_FOUND, """
          @|bold,red Error: Keyspace '%s' does not exist in database '%s'.|@

          To ignore this error, provide the @!--if-exists!@ flag to skip this error if the keyspace doesn't exist.
        """.formatted(
            $keyspaceRef.name(),
            $keyspaceRef.db()
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-exists"),
            new Hint("List existing keyspaces:", "astra db list-keyspaces %s".formatted($keyspaceRef.db()))
        ));
    }

    private Map<String, Object> mkData(Boolean wasDeleted, @Nullable Duration waitedDuration) {
        return Map.of(
            "wasDeleted", wasDeleted,
            "waitedSeconds", Optional.ofNullable(waitedDuration).map(Duration::getSeconds)
        );
    }
}
