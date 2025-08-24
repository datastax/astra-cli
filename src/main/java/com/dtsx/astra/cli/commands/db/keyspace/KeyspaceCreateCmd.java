package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceCreateOperation;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.core.output.ExitCode.KEYSPACE_ALREADY_EXISTS;
import static com.dtsx.astra.cli.operations.db.keyspace.KeyspaceCreateOperation.*;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "create-keyspace",
    description = "Create a new keyspace in the specified database"
)
@Example(
    comment = "Create a new keyspace",
    command = "${cli.name} db create-keyspace my_db -k my_keyspace"
)
@Example(
    comment = "Create a new keyspace without failing if it already exists",
    command = "${cli.name} db create-keyspace my_db -k my_keyspace --if-not-exists"
)
@Example(
    comment = "Create a new keyspace without waiting for the database to become active",
    command = "${cli.name} db create-keyspace my_db -k my_keyspace --async"
)
public class KeyspaceCreateCmd extends AbstractLongRunningKeyspaceRequiredCmd<KeyspaceCreateResult> {
    @Option(
        names = { "--if-not-exists" },
        description = { "Don't error if a keyspace with the same name already exists", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean $ifNotExists;

    @Option(names = LR_OPTS_TIMEOUT_NAME, description = LR_OPTS_TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    protected KeyspaceCreateOperation mkOperation() {
        return new KeyspaceCreateOperation(keyspaceGateway, dbGateway, new KeyspaceCreateRequest($keyspaceRef, $ifNotExists, lrMixin.options()));
    }

    @Override
    protected final OutputAll execute(Supplier<KeyspaceCreateResult> result) {
        return switch (result.get()) {
            case KeyspaceAlreadyExists() -> handleKeyspaceAlreadyExists();
            case KeyspaceCreated() -> handleKeyspaceCreated();
            case KeyspaceCreatedAndDbActive(var waitTime) -> handleKeyspaceCreatedAndDbActive(waitTime);
            case KeyspaceIllegallyAlreadyExists() -> throwKeyspaceAlreadyExists();
        };
    }

    private OutputAll handleKeyspaceAlreadyExists() {
        val message = "Keyspace %s already exists in database %s.".formatted(
            highlight($keyspaceRef),
            highlight($keyspaceRef.db())
        );

        val data = mkData(false, null);

        return OutputAll.response(message, data);
    }

    private OutputAll handleKeyspaceCreated() {
        val message = "Keyspace %s has been created in database %s (database may not be active yet).".formatted(
            highlight($keyspaceRef),
            highlight($keyspaceRef.db())
        );

        val data = mkData(true, null);

        return OutputAll.response(message, data, List.of(
            new Hint("Poll the database status:", "${cli.name} db status %s".formatted($keyspaceRef.db()))
        ));
    }

    private OutputAll handleKeyspaceCreatedAndDbActive(Duration waitTime) {
        val message = "Keyspace %s has been created in database %s.%n%nThe database is now active after waiting %d seconds.".formatted(
            highlight($keyspaceRef),
            highlight($keyspaceRef.db()),
            waitTime.toSeconds()
        );

        val data = mkData(true, waitTime);

        return OutputAll.response(message, data);
    }

    private <T> T throwKeyspaceAlreadyExists() {
        throw new AstraCliException(KEYSPACE_ALREADY_EXISTS, """
          @|bold,red Error: Keyspace '%s' already exists in database '%s'.|@
        
          To ignore this error, provide the @!--if-not-exists!@ flag to skip this error if the keyspace already exists.
        """.formatted(
            $keyspaceRef.name(),
            $keyspaceRef.db()
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-not-exists")
        ));
    }

    private LinkedHashMap<String, Object> mkData(Boolean wasCreated, @Nullable Duration waitedDuration) {
        return sequencedMapOf(
            "wasCreated", wasCreated,
            "waitedSeconds", Optional.ofNullable(waitedDuration).map(Duration::getSeconds)
        );
    }
}
