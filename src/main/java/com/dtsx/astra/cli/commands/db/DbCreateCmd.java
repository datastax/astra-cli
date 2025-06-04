package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.cli.output.output.OutputAll;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Optional;

@Command(
    name = "create"
)
public class DbCreateCmd extends AbstractLongRunningDbSpecificCmd {
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
            names = { "--vector" },
            description = { "Create a database with vector search enabled", DEFAULT_VALUE },
            defaultValue = "false"
        )
        protected boolean vector;
    }

    @Option(names = "--timeout", description = TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        this.timeout = Optional.of(timeout);
    }

    @Override
    public OutputAll execute() {
        val coloredDbName = AstraColors.BLUE_300.use(dbName);

        try {
            val database = AstraLogger.loading("Checking if database already exists", (_) -> (
                dbService.getDbInfo(dbName)
            ));

            AstraLogger.info("Database '%s' already exists with id %s'".formatted(dbName, database.getId()));

            val hadToBeResumed = dbService.resumeDb(dbName);
            val hadToBeAwaited = dbService.waitUntilDbActive(dbName, timeout.orElseThrow());

            val message =
                (hadToBeResumed)
                    ? "already existed with id %s, but needed to be resumed and is now active." :
                (hadToBeAwaited)
                    ? "already existed with id %s, and is now active."
                    : "already exists with id %s, and was already active; no action was required.";

            return OutputAll.message("Database " + coloredDbName + " " + message.formatted(AstraColors.BLUE_300.use(database.getId())));
        } catch (DatabaseNotFoundException _) {}

        AstraLogger.debug("Database '%s' does not already exist".formatted(coloredDbName));

        val cloudProvider = dbService.validateRegion(
            databaseCreationOptions.cloud,
            databaseCreationOptions.region,
            databaseCreationOptions.vector
        );

        val dbId = dbService.createDb(
            dbName,
            databaseCreationOptions.keyspace,
            databaseCreationOptions.region,
            cloudProvider,
            databaseCreationOptions.tier,
            databaseCreationOptions.capacityUnits,
            databaseCreationOptions.vector
        );

        if (async) {
            return OutputAll.message("Database %s has been created with id %s, but it may not be active yet.".formatted(coloredDbName, AstraColors.BLUE_300.use(dbId)));
        }

        dbService.waitUntilDbActive(dbName, timeout.orElseThrow());

        return OutputAll.message(
            "Database %s has been created with id %s and is now active.".formatted(coloredDbName, AstraColors.BLUE_300.use(dbId))
        );
    }
}
