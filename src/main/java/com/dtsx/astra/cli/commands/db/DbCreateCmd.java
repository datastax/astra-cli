package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.CliConstants.$Cloud;
import com.dtsx.astra.cli.core.CliConstants.$Keyspace;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.WithSetTimeout;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.db.DbCreateOperation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.*;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.ExitCode.DATABASE_ALREADY_EXISTS;
import static com.dtsx.astra.cli.operations.db.DbCreateOperation.*;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "create",
    description = "Create a new Astra database"
)
@Example(
    comment = "Create a basic Astra vector database in the 'us-east1' region",
    command = "${cli.name} db create my_db --region us-east1"
)
@Example(
    comment = "Specify a specific default keyspace for the database",
    command = "${cli.name} db create my_db --region us-east1 -k my_keyspace"
)
@Example(
    comment = "Create a classic non-vector database",
    command = "${cli.name} db create my_db --region us-east1 --non-vector"
)
@Example(
    comment = "Create a database without waiting for it to become active",
    command = "${cli.name} db create my_db --async"
)
@Example(
    comment = "Create a database if it doesn't already exist",
    command = "${cli.name} db create my_db --region us-east1 --if-not-exists"
)
@Example(
    comment = "List available vector database regions for creating a database",
    command = "${cli.name} db list-regions-vector"
)
public class DbCreateCmd extends AbstractDbRequiredCmd<DbCreateResult> implements WithSetTimeout {
    @ArgGroup(heading = "%nWhat to do if the database already exists:%n")
    public @Nullable ExistingBehavior $existingBehavior;

    public static class ExistingBehavior {
        @Option(
            names = { "--if-not-exists" },
            description = { "Don't error if the database already exists", DEFAULT_VALUE }
        )
        public boolean $ifNotExists;

        @Option(
            names = { "--allow-duplicate-names" },
            description = { "Allow multiple databases with the same name", DEFAULT_VALUE }
        )
        public boolean $allowDuplicateNames;
    }

    @ArgGroup(validate = false, heading = "%nDatabase configuration options:%n")
    public @Nullable DatabaseCreationOptions $databaseCreationOptions;

    @Mixin
    protected LongRunningOptionsMixin lrMixin;

    public static class DatabaseCreationOptions {
        @Option(
            names = { $Regions.LONG, $Regions.SHORT },
            description = "Cloud provider region to provision. @|bold Use one of the `${cli.name} db list-regions-*` commands to see available regions.|@",
            paramLabel = $Regions.LABEL
        )
        public RegionName region;

        @Option(
            names = { $Cloud.LONG, $Cloud.SHORT },
            description = "The cloud provider where the db should be created. Inferred from the region if not provided.",
            paramLabel = $Cloud.LABEL
        )
        public Optional<CloudProviderType> cloud;

        @Option(
            names = { $Keyspace.LONG, $Keyspace.SHORT },
            description = { "Default keyspace for the database", DEFAULT_VALUE },
            defaultValue = $Keyspace.DEFAULT,
            paramLabel = $Keyspace.LABEL
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

    @Option(names = LR_OPTS_TIMEOUT_NAME, description = LR_OPTS_TIMEOUT_DESC, defaultValue = "360")
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

        val existingBehavior =
            ($existingBehavior != null && $existingBehavior.$ifNotExists)
                ? DbCreateOperation.ExistingBehavior.CREATE_IF_NOT_EXISTS :
            ($existingBehavior != null && $existingBehavior.$allowDuplicateNames)
                ? DbCreateOperation.ExistingBehavior.ALLOW_DUPLICATES
                : DbCreateOperation.ExistingBehavior.FAIL;

        return new DbCreateOperation(dbGateway, new CreateDbRequest(
            dbName,
            $databaseCreationOptions.region,
            $databaseCreationOptions.cloud,
            $databaseCreationOptions.keyspace,
            $databaseCreationOptions.tier,
            $databaseCreationOptions.capacityUnits,
            $databaseCreationOptions.nonVector,
            existingBehavior,
            lrMixin.options()
        ));
    }

    @Override
    protected final OutputAll execute(Supplier<DbCreateResult> result) {
        return switch (result.get()) {
            case DatabaseAlreadyExistsWithStatus(var dbId, var currStatus) -> handleDbAlreadyExistsWithStatus(dbId, currStatus);
            case DatabaseAlreadyExistsIllegallyWithStatus(var dbId, var currStatus) -> throwDbAlreadyExistsWithStatus(dbId, currStatus);
            case DatabaseAlreadyExistsAndIsActive(var dbId, var prevStatus, var awaited) -> handleDbAlreadyExistsAndIsActive(dbId, prevStatus, awaited);
            case DatabaseCreationStarted(var dbId, var currStatus) -> handleDbCreationStarted(dbId, currStatus);
            case DatabaseCreated(var dbId, var waitTime) -> handleDbCreated(dbId, waitTime);
        };
    }

    private OutputAll handleDbAlreadyExistsWithStatus(UUID dbId, DatabaseStatusType currStatus) {
        val message = "Database %s already exists with id %s, and has status %s.".formatted(
            ctx.highlight($dbRef),
            ctx.highlight(dbId),
            ctx.highlight(currStatus)
        );

        val data = mkData(dbId, false, currStatus, null);

        return OutputAll.response(message, data, List.of(
            new Hint("Resume the database:", "${cli.name} db resume %s".formatted($dbRef)),
            new Hint("Get information about the existing database:", "${cli.name} db get %s".formatted($dbRef))
        ));
    }

    private <T> T throwDbAlreadyExistsWithStatus(UUID dbId, DatabaseStatusType currStatus) {
        throw new AstraCliException(DATABASE_ALREADY_EXISTS, """
          @|bold,red Error: Database %s already exists with id %s, and has status %s.|@
        
          To ignore this error, either:
          - Provide the @'!--if-not-exists!@ flag to skip this error if the database already exists.
          - Provide the @'!--allow-duplicate-names!@ flag to create the database even if another with the same name already exists.
        """.formatted(
            $dbRef,
            dbId,
            currStatus
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-not-exists"),
            new Hint("Example fix:", originalArgs(), "--allow-duplicate-names"),
            new Hint("Get information about the existing database:", "${cli.name} db get %s".formatted($dbRef))
        ));
    }

    private OutputAll handleDbAlreadyExistsAndIsActive(UUID dbId, DatabaseStatusType prevStatus, Duration awaited) {
        val message = awaited.isZero() 
            ? "Database %s already exists with id %s, and was already active; no action was required.".formatted(
                ctx.highlight($dbRef),
                ctx.highlight(dbId)
            )
            : "Database %s already exists with id %s, and had status %s. It is now active after waiting %s seconds.".formatted(
                ctx.highlight($dbRef),
                ctx.highlight(dbId),
                ctx.highlight(prevStatus),
                ctx.highlight(awaited.toSeconds())
            );

        val data = mkData(dbId, false, DatabaseStatusType.ACTIVE, awaited);

        return OutputAll.response(message, data, List.of(
            new Hint("Get more information about the existing database:", "${cli.name} db get %s".formatted($dbRef))
        ));
    }

    private OutputAll handleDbCreationStarted(UUID dbId, DatabaseStatusType currStatus) {
        val message = "Database %s has been created with id %s, and currently has status %s.".formatted(
            ctx.highlight($dbRef),
            ctx.highlight(dbId),
            ctx.highlight(currStatus)
        );

        val data = mkData(dbId, true, currStatus, null);

        return OutputAll.response(message, data, List.of(
            new Hint("Poll the database's status:", "${cli.name} db status %s".formatted($dbRef)),
            new Hint("Get more information about the new database:", "${cli.name} db get %s".formatted($dbRef))
        ));
    }

    private OutputAll handleDbCreated(UUID dbId, Duration waitTime) {
        val message = "Database %s has been created with id %s. It is now active after waiting %s seconds.".formatted(
            ctx.highlight($dbRef),
            ctx.highlight(dbId),
            ctx.highlight(waitTime.toSeconds())
        );

        val data = mkData(dbId, true, DatabaseStatusType.ACTIVE, waitTime);

        return OutputAll.response(message, data, List.of(
            new Hint("Get more information about the new database:", "${cli.name} db get %s".formatted($dbRef))
        ));
    }

    private LinkedHashMap<String, Object> mkData(UUID dbId, Boolean wasCreated, DatabaseStatusType currentStatus, @Nullable Duration waitedDuration) {
        return sequencedMapOf(
            "dbId", dbId,
            "wasCreated", wasCreated,
            "currentStatus", currentStatus,
            "waitedSeconds", Optional.ofNullable(waitedDuration).map(Duration::getSeconds)
        );
    }
}
