package com.dtsx.astra.cli.domain.db.keyspaces;

import com.dtsx.astra.cli.domain.db.DbRef;
import com.dtsx.astra.cli.exceptions.db.KeyspaceNotFoundException;
import com.dtsx.astra.cli.output.AstraLogger;
import lombok.RequiredArgsConstructor;

import static com.dtsx.astra.cli.output.AstraColors.highlight;

@RequiredArgsConstructor
public class KeyspaceServiceImpl implements KeyspaceService {
    private final KeyspaceDao ksDao;

    @Override
    public KeyspaceDao.FoundKeyspaces listKeyspaces(DbRef dbRef) {
        return AstraLogger.loading("Fetching keyspaces for db " + highlight(dbRef), (_) ->
            ksDao.findAll(dbRef)
        );
    }

    @Override
    public boolean keyspaceExists(KeyspaceRef keyspaceRef) {
        try {
            listKeyspaces(keyspaceRef.dbRef()).keyspaces().stream()
                .filter(ks -> ks.getName().equals(keyspaceRef.keyspaceName()))
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
            ksDao.create(keyspaceRef);
            return null;
        });
    }

    @Override
    public void deleteKeyspace(KeyspaceRef keyspaceRef) {
        AstraLogger.loading("Deleting keyspace " + highlight(keyspaceRef), (_) -> {
            ksDao.delete(keyspaceRef);
            return null;
        });
    }
}
