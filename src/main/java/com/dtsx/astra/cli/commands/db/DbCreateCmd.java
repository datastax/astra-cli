package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.DbCreateOperation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.DATABASE_ALREADY_EXISTS;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.DbCreateOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "create",
    description = "Create a new Astra database"
)
@Example(
    comment = "Create a basic Astra vector database in the 'us-east-1' region",
    command = "astra db create my_db --region us-east-1"
)
@Example(
    comment = "Specify a specific default keyspace for the database",
    command = "astra db create my_db --region us-east-1 -k my_keyspace"
)
@Example(
    comment = "Create a classic non-vector database",
    command = "astra db create my_db --region us-east-1 --non-vector"
)
@Example(
    comment = "Create a database without waiting for it to become active",
    command = "astra db create my_db --async"
)
@Example(
    comment = "Create a database if it doesn't already exist",
    command = "astra db create my_db --region us-east-1 --if-not-exists"
)
public class DbCreateCmd extends AbstractLongRunningDbSpecificCmd<DbCreateResult> {
    @Option(
        names = { "--if-not-exists" },
        description = { "Don't error if the database already exists", DEFAULT_VALUE }
    )
    public boolean $ifNotExists;

    @ArgGroup(validate = false, heading = "%nDatabase configuration options:%n")
    public @Nullable DatabaseCreationOptions $databaseCreationOptions;

    public static class DatabaseCreationOptions {
        @Option(
            names = { "-r", "--region" },
            paramLabel = "DB_REGION",
            description = "Cloud provider region to provision",
            required = true
        )
        public RegionName region;

        @Option(
            names = { "-c", "--cloud" },
            description = "The cloud provider where the db should be created. Inferred from the region if not provided."
        )
        public Optional<CloudProviderType> cloud;

        @Option(
            names = { "-k", "--keyspace" },
            paramLabel = "KEYSPACE",
            description = { "Default keyspace for the database", DEFAULT_VALUE },
            defaultValue = "default_keyspace"
        )
        public String keyspace;

        @Option(
            names = { "--tier" },
            paramLabel = "TIER",
            description = { "Tier to create the database in", DEFAULT_VALUE },
            defaultValue = "serverless"
        )
        public String tier;

        @Option(
            names = { "--capacity-units" },
            paramLabel = "CAPACITY UNITS",
            description = { "Capacity units to create the database with", DEFAULT_VALUE },
            defaultValue = "1"
        )
        public Integer capacityUnits;

        @Option(
            names = { "--non-vector" },
            description = { "Create a classic non-vector database", DEFAULT_VALUE },
            defaultValue = "false"
        )
        public boolean nonVector;
    }

    @Option(names = LR_OPTS_TIMEOUT_NAME, description = LR_OPTS_TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    protected DbCreateOperation mkOperation() {
        val dbName = $dbRef.fold(
            id -> { throw new OptionValidationException("database name", "may not provide an id (%s) when creating a new database; must be a human-readable database name".formatted(id.toString())); },
            name -> name
        );

        if ($databaseCreationOptions == null) {
            throw new ParameterException(spec.commandLine(), "Must provide a region (via --region) when creating a new database");
        }

        return new DbCreateOperation(dbGateway, new CreateDbRequest(
            dbName,
            $databaseCreationOptions.region,
            $databaseCreationOptions.cloud,
            $databaseCreationOptions.keyspace,
            $databaseCreationOptions.tier,
            $databaseCreationOptions.capacityUnits,
            $databaseCreationOptions.nonVector,
            $ifNotExists,
            lrMixin.options()
        ));
    }

    @Override
    protected final OutputAll execute(DbCreateResult result) {
        val message = switch (result) {
            case DatabaseAlreadyExistsWithStatus(var dbId, var currStatus) -> handleDbAlreadyExistsWithStatus(dbId, currStatus);
            case DatabaseAlreadyExistsIllegallyWithStatus(var dbId, var currStatus) -> throwDbAlreadyExistsWithStatus(dbId, currStatus);
            case DatabaseAlreadyExistsAndIsActive(var dbId, var prevStatus, var awaited) -> handleDbAlreadyExistsAndIsActive(dbId, prevStatus, awaited);
            case DatabaseCreationStarted(var dbId, var currStatus) -> handleDbCreationStarted(dbId, currStatus);
            case DatabaseCreated(var dbId, var waitTime) -> handleDbCreated(dbId, waitTime);
        };

        return OutputAll.message(trimIndent(message));
    }

    private String handleDbAlreadyExistsWithStatus(UUID dbId, DatabaseStatusType currStatus) {
        return """
          Database %s already exists with id %s, and has status %s.
        
          %s
          %s
        
          %s
          %s
        """.formatted(
            highlight($dbRef),
            highlight(dbId),
            highlight(currStatus),
            renderComment("Resume the database:"),
            renderCommand("astra db resume %s".formatted($dbRef)),
            renderComment("Get information about the existing database:"),
            renderCommand("astra db get %s".formatted($dbRef))
        );
    }

    private String throwDbAlreadyExistsWithStatus(UUID dbId, DatabaseStatusType currStatus) {
        throw new AstraCliException(DATABASE_ALREADY_EXISTS, """
          @|bold,red Error: Database %s already exists with id %s, and has status %s.|@
        
          To ignore this error, provide the %s flag to skip this error if the database already exists.
        
          %s
          %s
        
          %s
          %s
        """.formatted(
            $dbRef,
            dbId,
            currStatus,
            highlight("--if-not-exists"),
            renderComment("Example fix:"),
            renderCommand(originalArgs(), "--if-not-exists"),
            renderComment("Get information about the existing database:"),
            renderCommand("astra db get %s".formatted($dbRef))
        ));
    }

    private String handleDbAlreadyExistsAndIsActive(UUID dbId, DatabaseStatusType prevStatus, Duration awaited) {
        if (awaited.isZero()) {
            return """
              Database %s already exists with id %s, and was already active; no action was required.
           
              %s
              %s
            """.formatted(
                highlight($dbRef),
                highlight(dbId),
                renderComment("Get more information about the existing database:"),
                renderCommand("astra db get %s".formatted($dbRef))
            );
        }

        return """
          Database %s already exists with id %s, and had status %s.
        
          It is now active after waiting %s seconds.
        
          %s
          %s
        """.formatted(
            highlight($dbRef),
            highlight(dbId),
            highlight(prevStatus),
            highlight(awaited.toSeconds()),
            renderComment("Get more information about the existing database:"),
            renderCommand("astra db get %s".formatted($dbRef))
        );
    }

    private String handleDbCreationStarted(UUID dbId, DatabaseStatusType currStatus) {
        return """
          Database %s has been created with id %s, and currently has status %s.
        
          %s
          %s
        
          %s
          %s
        """.formatted(
            highlight($dbRef),
            highlight(dbId),
            highlight(currStatus),
            renderComment("Poll the database's status:"),
            renderCommand("astra db status %s".formatted($dbRef)),
            renderComment("Get more information about the new database:"),
            renderCommand("astra db get %s".formatted($dbRef))
        );
    }

    private String handleDbCreated(UUID dbId, Duration waitTime) {
        return """
          Database %s has been created with id %s.
        
          It is now active after waiting %s seconds.
        
          %s
          %s
        """.formatted(
            highlight($dbRef),
            highlight(dbId),
            highlight(waitTime.toSeconds()),
            renderComment("Get more information about the new database:"),
            renderCommand("astra db get %s".formatted($dbRef))
        );
    }
}
