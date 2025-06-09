package com.dtsx.astra.cli.domain.db.keyspaces;

import com.dtsx.astra.cli.domain.APIProvider;
import com.dtsx.astra.cli.domain.db.DbDao;
import com.dtsx.astra.cli.domain.db.DbRef;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface KeyspaceDao {
    static KeyspaceDao mkDefault(String token, AstraEnvironment env, DbDao dbDao) {
        return new KeyspaceDaoImpl(APIProvider.mkDefault(token, env), dbDao);
    }

    record FoundKeyspaces(
        @Nullable String defaultKeyspace,
        List<String> keyspaces
    ) {}

    FoundKeyspaces findAll(DbRef ref);
    
    void create(KeyspaceRef keyspaceRef);
    
    void delete(KeyspaceRef keyspaceRef);
}
