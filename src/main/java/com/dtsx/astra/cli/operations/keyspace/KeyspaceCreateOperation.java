package com.dtsx.astra.cli.operations.keyspace;

import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.gateways.keyspace.KeyspaceGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KeyspaceCreateOperation {
    private final KeyspaceGateway keyspaceGateway;

    public record KeyspaceCreateRequest(
        KeyspaceRef keyspaceRef,
        boolean ifNotExists
    ) {}

    public sealed interface KeyspaceCreateResult {
        record KeyspaceAlreadyExists(KeyspaceRef keyspaceRef) implements KeyspaceCreateResult {}
        record KeyspaceCreated(KeyspaceRef keyspaceRef) implements KeyspaceCreateResult {}
    }

    public static class KeyspaceAlreadyExistsException extends OptionValidationException {
        public KeyspaceAlreadyExistsException(KeyspaceRef keyspaceRef) {
            super("keyspace", "Keyspace '%s' already exists. Use --if-not-exists to ignore this error".formatted(keyspaceRef.name()));
        }
    }

    public KeyspaceCreateResult execute(KeyspaceCreateRequest request) {
        boolean exists = keyspaceGateway.keyspaceExists(request.keyspaceRef);
        
        if (exists) {
            if (request.ifNotExists) {
                return new KeyspaceCreateResult.KeyspaceAlreadyExists(request.keyspaceRef);
            } else {
                throw new KeyspaceAlreadyExistsException(request.keyspaceRef);
            }
        }

        keyspaceGateway.createKeyspace(request.keyspaceRef);
        return new KeyspaceCreateResult.KeyspaceCreated(request.keyspaceRef);
    }
}
