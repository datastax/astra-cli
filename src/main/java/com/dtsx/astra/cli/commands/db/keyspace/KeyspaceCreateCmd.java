package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.keyspace.KeyspaceCreateOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.KEYSPACE_ALREADY_EXISTS;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.keyspace.KeyspaceCreateOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "create-keyspace",
    description = "Create a new keyspace in the specified database"
)
@Example(
    comment = "Create a new keyspace",
    command = "astra db create-keyspace my_db -k my_keyspace"
)
@Example(
    comment = "Create a new keyspace without failing if it already exists",
    command = "astra db create-keyspace my_db -k my_keyspace --if-not-exists"
)
@Example(
    comment = "Create a new keyspace without waiting for the database to become active",
    command = "astra db create-keyspace my_db -k my_keyspace --async"
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
    protected final OutputAll execute(KeyspaceCreateResult result) {
        val message = switch (result) {
            case KeyspaceAlreadyExists() -> handleKeyspaceAlreadyExists();
            case KeyspaceCreated() -> handleKeyspaceCreated();
            case KeyspaceCreatedAndDbActive(var waitTime) -> handleKeyspaceCreatedAndDbActive(waitTime);
            case KeyspaceIllegallyAlreadyExists() -> throwKeyspaceAlreadyExists();
        };

        return OutputAll.message(trimIndent(message));
    }

    private String handleKeyspaceAlreadyExists() {
        return "Keyspace " + highlight($keyspaceRef) + " already exists in database " + highlight($keyspaceRef.db()) + ".";
    }

    private String handleKeyspaceCreated() {
        return """
          Keyspace %s has been created in database %s (database may not be active yet).
        
          %s
          %s
        """.formatted(
            highlight($keyspaceRef),
            highlight($keyspaceRef.db()),
            renderComment("Poll the database status:"),
            renderCommand("astra db status %s".formatted($keyspaceRef.db()))
        );
    }

    private String handleKeyspaceCreatedAndDbActive(Duration waitTime) {
        return """
          Keyspace %s has been created in database %s.
        
          The database is now active after waiting %d seconds.
        """.formatted(
            highlight($keyspaceRef),
            highlight($keyspaceRef.db()),
            waitTime.toSeconds()
        );
    }

    private String throwKeyspaceAlreadyExists() {
        throw new AstraCliException(KEYSPACE_ALREADY_EXISTS, """
          @|bold,red Error: Keyspace '%s' already exists in database '%s'.|@
        
          To ignore this error, provide the %s flag to skip this error if the keyspace already exists.
        
          %s
          %s
        """.formatted(
            $keyspaceRef.name(),
            $keyspaceRef.db(),
            highlight("--if-not-exists"),
            renderComment("Example fix:"),
            renderCommand(originalArgs(), "--if-not-exists")
        ));
    }
}
