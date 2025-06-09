package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.domain.db.DbRef;
import com.dtsx.astra.cli.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.cli.output.output.OutputAll;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.output.AstraColors.highlight;
import static com.dtsx.astra.cli.output.AstraColors.highlightStatus;

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

    @Override
    public OutputAll execute() {
        val database = AstraLogger.loading("Checking if database exists", (_) -> (
            dbService.tryGetDbInfo(dbRef)
        ));

        return database
            .<Supplier<OutputAll>>map(DbAlreadyExistsHandler::new)
            .orElseGet(CreateNewDbHandler::new)
            .get();
    }

    @RequiredArgsConstructor
    private class DbAlreadyExistsHandler implements Supplier<OutputAll> {
        private final Database db;

        @Override
        public OutputAll get() {
            return
                (!ifNotExists)
                    ? throwDbAlreadyExists() :
                (dontWait)
                    ? returnCurrentStatus()
                    : resumeDbAndWait();
        }

        private <T> T throwDbAlreadyExists() {
            throw new OptionValidationException("database name", "Database %s already exists with id %s".formatted(highlight(dbRef), highlight(db.getId())));
        }

        private OutputAll returnCurrentStatus() {
            return mkDbExistsMsg(" and status " + highlightStatus(db.getStatus()));
        }

        private OutputAll resumeDbAndWait() {
            val resumeResult = dbService.resumeDb(dbRef, timeout);

            if (!resumeResult.wasAwaited()) {
                return mkDbExistsMsg(", and was already active; no action was required.");
            }

            val status = highlightStatus(db.getStatus());
            val resumeStatus = (resumeResult.hadToBeResumed()) ? " and needed to be resumed" : "";
            val timeWaited = highlight(resumeResult.timeWaited().toSeconds());

            return mkDbExistsMsg(
                ", but had status %s%s. It is now active after waiting %s seconds.".formatted(status, resumeStatus, timeWaited)
            );
        }

        private OutputAll mkDbExistsMsg(String rest) {
            return OutputAll.message("Database %s already exists with id %s".formatted(highlight(dbRef), highlight(db.getId())) + rest);
        }
    }

    private class CreateNewDbHandler implements Supplier<OutputAll> {
        @Override
        public OutputAll get() {
            val dbName = dbRef.fold(
                id -> { throw new OptionValidationException("database name", "may not provide an id (%s) when creating a new database; must be a human-readable database name".formatted(id.toString())); },
                name -> name
            );

            val createdId = createDb(dbName);

            return (dontWait)
                ? returnCurrentStatus(dbName, createdId)
                : resumeDbAndWait(dbName, createdId);
        }

        private UUID createDb(String dbName) {
            val cloudProvider = dbService.findCloudForRegion(
                databaseCreationOptions.cloud,
                databaseCreationOptions.region,
                !databaseCreationOptions.nonVector
            );

            return dbService.createDb(
                dbName,
                databaseCreationOptions.keyspace,
                databaseCreationOptions.region,
                cloudProvider,
                databaseCreationOptions.tier,
                databaseCreationOptions.capacityUnits,
                !databaseCreationOptions.nonVector
            );
        }

        private OutputAll returnCurrentStatus(String dbName, UUID dbId) {
            val db = dbService.getDbInfo(DbRef.fromId(dbId));

            return OutputAll.message("Database %s has been created with id %s, and currently has status %s".formatted(
                highlight(dbName), highlight(dbId.toString()), highlightStatus(db.getStatus())
            ));
        }

        private OutputAll resumeDbAndWait(String dbName, UUID dbId) {
            val awaitedDuration = dbService.waitUntilDbActive(dbRef, timeout);

            return OutputAll.message(
                "Database %s has been created with id %s, and is now active after %s seconds.".formatted(highlight(dbName), highlight(dbId.toString()), highlight(awaitedDuration.toSeconds()))
            );
        }
    }
}
