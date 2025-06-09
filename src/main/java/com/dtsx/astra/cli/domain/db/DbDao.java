package com.dtsx.astra.cli.domain.db;

import com.dtsx.astra.cli.domain.GlobalInfoCache;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;
import java.util.UUID;

public interface DbDao {
    static DbDao mkDefault(String token, AstraEnvironment env) {
        return new DbDaoImpl(token, env, GlobalInfoCache.INSTANCE);
    }

    List<Database> findAll();

    Database findOne(DbRef ref);

    void resume(DbRef ref);

    UUID create(String name, String keyspace, String region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector);
}
