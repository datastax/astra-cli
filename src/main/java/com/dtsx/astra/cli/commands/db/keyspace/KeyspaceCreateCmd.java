package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.cli.output.output.OutputAll;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.output.AstraColors.highlight;

@Command(
    name = "create"
)
public class KeyspaceCreateCmd extends AbstractKeyspaceRequiredCmd {
    @Option(
        names = { "--if-not-exists" },
        description = { "Will create a new keyspace only if none with same name", DEFAULT_VALUE },
        defaultValue = "false"
    )
    protected boolean ifNotExists;

    @Override
    public OutputAll execute() {
        if (keyspaceService.keyspaceExists(keyspaceRef)) {
            if (ifNotExists) {
                return OutputAll.message("Keyspace " + highlight(keyspaceRef) + " already exists");
            } else {
                throw new OptionValidationException("keyspace", "Keyspace '%s' already exists. Use --if-not-exists to ignore this error".formatted(keyspaceRef.name()));
            }
        }

        keyspaceService.createKeyspace(keyspaceRef);

        return OutputAll.message(
            "Keyspace %s has been created in database %s".formatted(
                highlight(keyspaceRef),
                highlight(keyspaceRef.getDatabaseName())
            )
        );
    }
}
