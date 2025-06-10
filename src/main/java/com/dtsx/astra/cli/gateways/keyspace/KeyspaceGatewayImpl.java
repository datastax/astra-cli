package com.dtsx.astra.cli.gateways.keyspace;

import com.dtsx.astra.cli.core.exceptions.db.DbNotFoundException;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.APIProviderImpl;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.exceptions.db.KeyspaceNotFoundException;
import com.dtsx.astra.cli.gateways.db.DbGatewayImpl;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
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
        try {
            findAllKeyspaces(keyspaceRef.db()).keyspaces().stream()
                .filter(ks -> ks.equals(keyspaceRef.name()))
                .findFirst()
                .orElseThrow(() -> new KeyspaceNotFoundException(keyspaceRef));
            return true;
        } catch (KeyspaceNotFoundException e) {
            return false;
        }
    }

    @Override
    public void createKeyspace(KeyspaceRef keyspaceRef) {
        AstraLogger.loading("Creating keyspace " + highlight(keyspaceRef), (_) -> {
            api.dbOpsClient(keyspaceRef.db()).keyspaces().create(keyspaceRef.name());
            return null;
        });
    }

    @Override
    public void deleteKeyspace(KeyspaceRef keyspaceRef) {
        AstraLogger.loading("Deleting keyspace " + highlight(keyspaceRef), (_) -> {
            api.dbOpsClient(keyspaceRef.db()).keyspaces().delete(keyspaceRef.name());
            return null;
        });
    }
}
