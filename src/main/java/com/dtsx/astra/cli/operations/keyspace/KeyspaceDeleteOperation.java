package com.dtsx.astra.cli.operations.keyspace;

import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.gateways.keyspace.KeyspaceGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KeyspaceDeleteOperation {
    private final KeyspaceGateway keyspaceGateway;

    public record KeyspaceDeleteRequest(
        KeyspaceRef keyspaceRef,
        boolean ifExists
    ) {}

    public sealed interface KeyspaceDeleteResult {
        record KeyspaceNotFound(KeyspaceRef keyspaceRef) implements KeyspaceDeleteResult {}
        record KeyspaceDeleted(KeyspaceRef keyspaceRef) implements KeyspaceDeleteResult {}
    }

    public static class KeyspaceNotFoundException extends OptionValidationException {
        public KeyspaceNotFoundException(KeyspaceRef keyspaceRef) {
            super("keyspace", "Keyspace '%s' does not exist. Use --if-exists to ignore this error".formatted(keyspaceRef.name()));
        }
    }

    public KeyspaceDeleteResult execute(KeyspaceDeleteRequest request) {
        var foundKeyspaces = keyspaceGateway.findAllKeyspaces(request.keyspaceRef.db());
        boolean exists = foundKeyspaces.keyspaces().contains(request.keyspaceRef.name());
        
        if (!exists) {
            if (request.ifExists) {
                return new KeyspaceDeleteResult.KeyspaceNotFound(request.keyspaceRef);
            } else {
                throw new KeyspaceNotFoundException(request.keyspaceRef);
            }
        }

        keyspaceGateway.deleteKeyspace(request.keyspaceRef);
        return new KeyspaceDeleteResult.KeyspaceDeleted(request.keyspaceRef);
    }
}
