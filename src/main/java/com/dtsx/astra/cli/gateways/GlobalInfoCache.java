package com.dtsx.astra.cli.gateways;

import com.dtsx.astra.cli.gateways.db.DbCache;
import com.dtsx.astra.cli.core.models.DbRef;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public enum GlobalInfoCache implements DbCache {
    INSTANCE;

    private final Map<String, @NotNull UUID> dbIdCache = new HashMap<>();
    private final Map<UUID, @NotNull String> dbRegionCache = new HashMap<>();

    @Override
    public void cacheDbId(String dbName, UUID id) {
        dbIdCache.put(dbName, id);
    }

    @Override
    public void cacheDbRegion(UUID id, String region) {
        dbRegionCache.put(id, region);
    }

    @Override
    public Optional<UUID> lookupDbId(DbRef ref) {
        return ref.fold(Optional::of, name -> Optional.ofNullable(dbIdCache.get(name)));
    }

    @Override
    public Optional<String> lookupDbRegion(DbRef ref) {
        return ref.fold(
            id -> Optional.ofNullable(dbRegionCache.get(id)),
            name -> Optional.ofNullable(dbIdCache.get(name)).map(dbRegionCache::get)
        );
    }
}
