package com.dtsx.astra.cli.gateways.db.keyspace;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.db.DbNotFoundException;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class KeyspaceGatewayImpl implements KeyspaceGateway {
    private final APIProvider api;

    @Override
    public FoundKeyspaces findAllKeyspaces(DbRef dbRef) {
        return AstraLogger.loading("Fetching keyspaces for db " + highlight(dbRef), (_) -> {
            val db = api.dbOpsClient(dbRef).find().orElseThrow(() -> new DbNotFoundException(dbRef));

            val defaultKeyspace = db.getInfo().getKeyspace();

            if (db.getInfo().getKeyspaces() == null) {
                return new FoundKeyspaces(defaultKeyspace, java.util.List.of());
            }

            return new FoundKeyspaces(defaultKeyspace, new ArrayList<>(db.getInfo().getKeyspaces()));
        });
    }

    @Override
    public boolean keyspaceExists(KeyspaceRef keyspaceRef) {
        return AstraLogger.loading("Checking if keyspace " + highlight(keyspaceRef) + " exists", (_) -> (
            findAllKeyspaces(keyspaceRef.db()).keyspaces().stream()
                .anyMatch(ks -> ks.equals(keyspaceRef.name()))
        ));
    }

    @Override
    public CreationStatus<KeyspaceRef> createKeyspace(KeyspaceRef keyspaceRef) {
        if (keyspaceExists(keyspaceRef)) {
            return CreationStatus.alreadyExists(keyspaceRef);
        }

        AstraLogger.loading("Creating keyspace " + highlight(keyspaceRef), (_) -> {
            api.dbOpsClient(keyspaceRef.db()).keyspaces().create(keyspaceRef.name());
            return null;
        });

        return CreationStatus.created(keyspaceRef);
    }

    @Override
    public DeletionStatus<KeyspaceRef> deleteKeyspace(KeyspaceRef keyspaceRef) {
        if (!keyspaceExists(keyspaceRef)) {
            return DeletionStatus.notFound(keyspaceRef);
        }

        AstraLogger.loading("Deleting keyspace " + highlight(keyspaceRef), (_) -> {
            api.dbOpsClient(keyspaceRef.db()).keyspaces().delete(keyspaceRef.name());
            return null;
        });

        return DeletionStatus.deleted(keyspaceRef);
    }
}
