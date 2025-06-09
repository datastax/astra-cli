package com.dtsx.astra.cli.domain.db.keyspaces;

import com.dtsx.astra.cli.domain.db.DbDao;
import com.dtsx.astra.cli.domain.db.DbRef;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

public interface KeyspaceService {
    static KeyspaceService mkDefault(String token, AstraEnvironment env) {
        return new KeyspaceServiceImpl(KeyspaceDao.mkDefault(token, env, DbDao.mkDefault(token, env)));
    }

    KeyspaceDao.FoundKeyspaces listKeyspaces(DbRef dbRef);
    
    boolean keyspaceExists(KeyspaceRef keyspaceRef);
    
    void createKeyspace(KeyspaceRef keyspaceRef);
    
    void deleteKeyspace(KeyspaceRef keyspaceRef);
}
