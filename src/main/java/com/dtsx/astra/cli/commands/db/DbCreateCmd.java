package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.cli.output.output.OutputAll;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.DatabaseCreationBuilder;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.jetbrains.annotations.Nullable;

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
    private @Nullable DatabaseCreationOptions databaseCreationOptions;

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
            description = "Cloud Provider to create a db",
            required = true
        )
        protected CloudProviderType cloud;

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
            description = "Tier to create the database in"
        )
        protected String tier = DatabaseCreationBuilder.DEFAULT_TIER;

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

    @Option(
        names = "--timeout",
        description = TIMEOUT_DESC,
        defaultValue = "600"
    )
    public void setTimeout(int timeout) {
        this.timeout = Optional.of(timeout);
    }

    @Override
    public OutputAll execute() {
        AstraLogger.loading("Creating database", null, (updateMsg) -> {
            try {
                Thread.sleep(1500);
                updateMsg.accept("Step 1 done...");
                Thread.sleep(1500);
                updateMsg.accept("Step 2 done...");
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return null;
        });

        return OutputAll.serializeValue(String.valueOf(timeout));
    }
}
