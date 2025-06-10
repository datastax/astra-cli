package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.db.DbCreateOperation;
import com.dtsx.astra.cli.operations.db.DbCreateOperation.*;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "create"
)
public final class DbCreateCmd extends AbstractLongRunningDbSpecificCmd {
    @Option(
        names = { "--if-not-exist", "--if-not-exists" },
        description = { "Don't error if the database already exists", DEFAULT_VALUE },
        defaultValue = "false"
    )
    protected boolean ifNotExists;

    @ArgGroup(validate = false, heading = "%nDatabase configuration options:%n")
    private DatabaseCreationOptions databaseCreationOptions;

    static class DatabaseCreationOptions {
        @Option(
            names = { "-r", "--region" },
            paramLabel = "DB_REGION",
            description = "Cloud provider region to provision",
            required = true
        )
        protected String region;

        @Option(
            names = { "-c", "--cloud" },
            description = "Cloud Provider to create a db"
        )
        protected Optional<CloudProviderType> cloud;

        @Option(
            names = { "-k", "--keyspace" },
            paramLabel = "KEYSPACE",
            description = { "Default keyspace for the database", DEFAULT_VALUE },
            defaultValue = "default_keyspace"
        )
        protected String keyspace;

        @Option(
            names = { "--tier" },
            paramLabel = "TIER",
            description = "Tier to create the database in",
            defaultValue = "serverless"
        )
        protected String tier;

        @Option(
            names = { "--capacity-units" },
            paramLabel = "CAPACITY UNITS",
            description = { "Capacity units to create the database with", DEFAULT_VALUE },
            defaultValue = "1"
        )
        protected Integer capacityUnits;

        @Option(
            names = { "--non-vector" },
            description = { "Create a non-vector database", DEFAULT_VALUE },
            defaultValue = "false"
        )
        protected boolean nonVector;
    }

    @Option(names = "--timeout", description = TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private DbCreateOperation dbCreateOperation;

    @Override
    protected void prelude() {
        super.prelude();
        this.dbCreateOperation = new DbCreateOperation(dbGateway);
    }

    @Override
    public OutputAll execute() {
        val dbName = dbRef.fold(
            id -> { throw new OptionValidationException("database name", "may not provide an id (%s) when creating a new database; must be a human-readable database name".formatted(id.toString())); },
            name -> name
        );

        val result = dbCreateOperation.execute(new CreateDbRequest(
            dbName,
            databaseCreationOptions.region,
            databaseCreationOptions.cloud,
            databaseCreationOptions.keyspace,
            databaseCreationOptions.tier,
            databaseCreationOptions.capacityUnits,
            databaseCreationOptions.nonVector,
            ifNotExists,
            dontWait,
            timeout
        ));

        val message = switch (result) {
            case DatabaseAlreadyExistsWithStatus(var dbId, var currStatus) -> handleDbAlreadyExistsWithStatus(dbId, currStatus);
            case DatabaseAlreadyExistsAndIsNowActive(var dbId, var prevStatus, var resumeResult) -> handleDbAlreadyExistsAndIsNowActive(dbId, prevStatus, resumeResult);
            case DatabaseCreationStarted(var dbId, var currStatus) -> handleDbCreationStarted(dbId, currStatus);
            case DatabaseCreated(var dbId, var waitTime) -> handleDbCreated(dbId, waitTime);
        };

        return OutputAll.message(message);
    }

    private String handleDbAlreadyExistsWithStatus(UUID dbId, DatabaseStatusType currStatus) {
        return "Database %s already exists with id %s, and has status %s.".formatted(highlight(dbRef), highlight(dbId), highlight(currStatus));
    }

    private String handleDbAlreadyExistsAndIsNowActive(UUID dbId, DatabaseStatusType prevStatus, DbGateway.ResumeDbResult resumeResult) {
        if (!resumeResult.wasAwaited()) {
            return "Database %s already exists with id %s, and was already active; no action was required.".formatted(highlight(dbRef), highlight(dbId));
        }
        return "Database %s already exists with id %s, but had currStatus %s%s. It is now active after waiting %s seconds.".formatted(highlight(dbRef), highlight(dbId), highlight(prevStatus), ((resumeResult.hadToBeResumed()) ? " and needed to be resumed" : ""), highlight(resumeResult.timeWaited().toSeconds()));
    }

    private String handleDbCreationStarted(UUID dbId, DatabaseStatusType currStatus) {
        return "Database %s has been created with id %s, and currently has status %s".formatted(highlight(dbRef), highlight(dbId), highlight(currStatus));
    }

    private String handleDbCreated(UUID dbId, Duration waitTime) {
        return "Database %s has been created with id %s, and is now active after %s seconds.".formatted(highlight(dbRef), highlight(dbId), highlight(waitTime.toSeconds()));
    }
}
