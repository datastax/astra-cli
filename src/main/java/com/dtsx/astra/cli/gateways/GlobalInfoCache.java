package com.dtsx.astra.cli.gateways;

import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.db.DbCache;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.pcu.PcuCache;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public enum GlobalInfoCache implements DbCache, PcuCache {
    INSTANCE;

    // Currently only db & pcu ids/regions are cached since they're realistically the only attributes
    // that may be need to be recalculated multiple times during a single CLI execution.
    private final Map<String, @NotNull UUID> dbIdCache = new HashMap<>();
    private final Map<UUID, @NotNull RegionName> dbRegionCache = new HashMap<>();
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
    public Optional<UUID> lookupPcuGroupId(PcuRef ref) {
        return ref.fold(Optional::of, name -> Optional.ofNullable(pcuGroupIdCache.get(name)));
    }
}
