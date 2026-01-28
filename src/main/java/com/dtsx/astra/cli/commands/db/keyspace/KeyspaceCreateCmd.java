package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.WithSetTimeout;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceCreateOperation;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DB_ACTIVE_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.ExitCode.KEYSPACE_ALREADY_EXISTS;
import static com.dtsx.astra.cli.operations.db.keyspace.KeyspaceCreateOperation.*;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

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
public class KeyspaceCreateCmd extends AbstractKeyspaceRequiredCmd<KeyspaceCreateResult> implements WithSetTimeout {
    @Option(
        names = { "--if-not-exists" },
        description = "Don't error if a keyspace with the same name already exists",
        defaultValue = "false"
    )
    public boolean $ifNotExists;

    @Mixin
    protected LongRunningOptionsMixin lrMixin;

    @Option(
        names = LR_OPTS_TIMEOUT_NAME,
        description = LR_OPTS_TIMEOUT_DB_ACTIVE_DESC,
        defaultValue = "1m"
    )
    public void setTimeout(Duration timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    protected Operation<KeyspaceCreateResult> mkOperation() {
        return new KeyspaceCreateOperation(keyspaceGateway, dbGateway, new KeyspaceCreateRequest($keyspaceRef, $ifNotExists, lrMixin.options(ctx)));
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
            ctx.highlight($keyspaceRef),
            ctx.highlight($keyspaceRef.db())
        );

        val data = mkData(false, null);

        return OutputAll.response(message, data);
    }

    private OutputAll handleKeyspaceCreated() {
        val message = "Keyspace %s has been created in database %s (database may not be active yet).".formatted(
            ctx.highlight($keyspaceRef),
            ctx.highlight($keyspaceRef.db())
        );

        val data = mkData(true, null);

        return OutputAll.response(message, data, List.of(
            new Hint("Poll the database status:", "${cli.name} db status %s".formatted($keyspaceRef.db()))
        ));
    }

    private OutputAll handleKeyspaceCreatedAndDbActive(Duration waitTime) {
        val message = "Keyspace %s has been created in database %s.%n%nThe database is now active after waiting %d seconds.".formatted(
            ctx.highlight($keyspaceRef),
            ctx.highlight($keyspaceRef.db()),
            waitTime.toSeconds()
        );

        val data = mkData(true, waitTime);

        return OutputAll.response(message, data);
    }

    private <T> T throwKeyspaceAlreadyExists() {
        throw new AstraCliException(KEYSPACE_ALREADY_EXISTS, """
          @|bold,red Error: Keyspace '%s' already exists in database '%s'.|@
        
          To ignore this error, provide the @'!--if-not-exists!@ flag to skip this error if the keyspace already exists.
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
