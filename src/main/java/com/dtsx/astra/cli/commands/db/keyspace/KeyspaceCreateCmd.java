package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.operations.keyspace.KeyspaceCreateOperation;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

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

    private KeyspaceCreateOperation keyspaceCreateOperation;

    @Override
    protected void prelude() {
        super.prelude();
        this.keyspaceCreateOperation = new KeyspaceCreateOperation(keyspaceGateway);
    }

    @Override
    public OutputAll execute() {
        val request = new KeyspaceCreateOperation.KeyspaceCreateRequest(keyspaceRef, ifNotExists);
        val result = keyspaceCreateOperation.execute(request);

        return switch (result) {
            case KeyspaceCreateOperation.KeyspaceCreateResult.KeyspaceAlreadyExists(var ksRef) -> {
                yield OutputAll.message("Keyspace " + highlight(ksRef) + " already exists");
            }
            case KeyspaceCreateOperation.KeyspaceCreateResult.KeyspaceCreated(var ksRef) -> {
                yield OutputAll.message(
                    "Keyspace %s has been created in database %s".formatted(
                        highlight(ksRef),
                        highlight(ksRef.db())
                    )
                );
            }
        };
    }
}
