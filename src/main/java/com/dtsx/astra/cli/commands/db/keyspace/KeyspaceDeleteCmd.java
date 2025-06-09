package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.exceptions.db.KeyspaceNotFoundException;
import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.cli.output.output.OutputAll;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.output.AstraColors.highlight;

@Command(
    name = "delete",
    aliases = { "rm" }
)
public class KeyspaceDeleteCmd extends AbstractKeyspaceRequiredCmd {
    
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if keyspace does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    protected boolean ifExists;

    @Override
    public OutputAll execute() {
        try {
            val existingKeyspaces = AstraLogger.loading("Checking if keyspace exists", (_) -> (
                keyspaceService.listKeyspaces(dbRef)
            ));

            if (!existingKeyspaces.keyspaces().contains(keyspaceRef.name())) {
                if (ifExists) {
                    AstraLogger.info("Keyspace '%s' does not exist".formatted(keyspaceRef.name()));
                    return OutputAll.message("Keyspace " + highlight(keyspaceRef) + " does not exist; nothing to delete");
                } else {
                    throw new OptionValidationException("keyspace", "Keyspace '%s' does not exist. Use --if-exists to ignore this error".formatted(keyspaceRef.name()));
                }
            }

            AstraLogger.debug("Keyspace %s exists, deleting it".formatted(highlight(keyspaceRef)));
            
            keyspaceService.deleteKeyspace(keyspaceRef);
            
            return OutputAll.message(
                "Keyspace %s has been deleted from database %s".formatted(
                    highlight(keyspaceRef),
                    highlight(keyspaceRef.getDatabaseName())
                )
            );
        } catch (KeyspaceNotFoundException e) {
            if (ifExists) {
                AstraLogger.info("Keyspace '%s' does not exist".formatted(keyspaceRef.name()));
                return OutputAll.message("Keyspace " + highlight(keyspaceRef) + " does not exist; nothing to delete");
            } else {
                throw e;
            }
        }
    }
}