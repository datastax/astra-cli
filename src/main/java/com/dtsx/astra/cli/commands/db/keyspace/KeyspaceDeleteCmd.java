package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.operations.keyspace.KeyspaceDeleteOperation;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

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

    private KeyspaceDeleteOperation keyspaceDeleteOperation;

    @Override
    protected void prelude() {
        super.prelude();
        this.keyspaceDeleteOperation = new KeyspaceDeleteOperation(keyspaceGateway);
    }

    @Override
    public OutputAll execute() {
        val request = new KeyspaceDeleteOperation.KeyspaceDeleteRequest(keyspaceRef, ifExists);
        val result = keyspaceDeleteOperation.execute(request);

        return switch (result) {
            case KeyspaceDeleteOperation.KeyspaceDeleteResult.KeyspaceNotFound(var ksRef) -> {
                yield OutputAll.message("Keyspace " + highlight(ksRef) + " does not exist; nothing to delete");
            }
            case KeyspaceDeleteOperation.KeyspaceDeleteResult.KeyspaceDeleted(var ksRef) -> {
                yield OutputAll.message(
                    "Keyspace %s has been deleted from database %s".formatted(
                        highlight(ksRef),
                        highlight(ksRef.db())
                    )
                );
            }
        };
    }
}
