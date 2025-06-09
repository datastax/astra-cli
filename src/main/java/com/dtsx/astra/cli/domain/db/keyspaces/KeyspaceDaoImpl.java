package com.dtsx.astra.cli.domain.db.keyspaces;

import com.dtsx.astra.cli.domain.APIProvider;
import com.dtsx.astra.cli.domain.APIProviderImpl;
import com.dtsx.astra.cli.domain.db.DbDao;
import com.dtsx.astra.cli.domain.db.DbRef;
import com.dtsx.astra.cli.domain.db.DbService;
import com.dtsx.astra.cli.exceptions.db.KeyspaceNotFoundException;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class KeyspaceDaoImpl implements KeyspaceDao {
    private final APIProvider api;
    private final DbDao dbDao;

    @Override
    public FoundKeyspaces findAll(DbRef dbRef) {
        val db = dbDao.findOne(dbRef);

        val defaultKeyspace = db.getInfo().getKeyspace();

        if (db.getInfo().getKeyspaces() == null) {
            return new FoundKeyspaces(defaultKeyspace, List.of());
        }

        return new FoundKeyspaces(defaultKeyspace, new ArrayList<>(db.getInfo().getKeyspaces()));
    }

    @Override
    public void create(KeyspaceRef keyspaceRef) {
        ((APIProviderImpl) api).devopsApiDatabase(keyspaceRef).createKeyspace(keyspaceRef.name());
    }

    @Override
    public void delete(KeyspaceRef keyspaceRef) {
        val foundKeyspaces = findAll(keyspaceRef.db());
        if (!foundKeyspaces.keyspaces().contains(keyspaceRef.name())) {
            throw new KeyspaceNotFoundException(keyspaceRef);
        }
        ((APIProviderImpl) api).devopsApiDatabase(keyspaceRef).dropKeyspace(keyspaceRef.name());
    }
}
