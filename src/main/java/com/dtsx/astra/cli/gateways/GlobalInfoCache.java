package com.dtsx.astra.cli.gateways;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbCache;
import com.dtsx.astra.cli.gateways.pcu.PcuCache;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public enum GlobalInfoCache implements DbCache, PcuCache {
    INSTANCE;

    private final Map<String, @NotNull UUID> dbIdCache = new HashMap<>();
    private final Map<UUID, @NotNull RegionName> dbRegionCache = new HashMap<>();
    private final Map<UUID, @NotNull String> dbOnlyKsCache = new HashMap<>();
    private final Map<String, @NotNull UUID> pcuGroupIdCache = new HashMap<>();

    @Override
    public void cacheDbId(String dbName, UUID id) {
        dbIdCache.put(dbName, id);
    }

    @Override
    public void cacheDbRegion(UUID id, RegionName region) {
        dbRegionCache.put(id, region);
    }

    @Override
    public void cacheDbDefaultKs(UUID id, String keyspace) {
        dbOnlyKsCache.put(id, keyspace);
    }

    @Override
    public void cachePcuGroupId(String title, UUID id) {
        pcuGroupIdCache.put(title, id);
    }

    @Override
    public Optional<UUID> lookupDbId(DbRef ref) {
        return ref.fold(Optional::of, name -> Optional.ofNullable(dbIdCache.get(name)));
    }

    @Override
    public Optional<RegionName> lookupDbRegion(DbRef ref) {
        return ref.fold(
            id -> Optional.ofNullable(dbRegionCache.get(id)),
            name -> Optional.ofNullable(dbIdCache.get(name)).map(dbRegionCache::get)
        );
    }

    @Override
    public Optional<String> lookupDbDefaultKs(DbRef ref) {
        return ref.fold(
            id -> Optional.ofNullable(dbOnlyKsCache.get(id)),
            name -> Optional.ofNullable(dbIdCache.get(name)).map(dbOnlyKsCache::get)
        );
    }

    @Override
    public Optional<UUID> lookupPcuGroupId(PcuRef ref) {
        return ref.fold(Optional::of, name -> Optional.ofNullable(pcuGroupIdCache.get(name)));
    }
}
