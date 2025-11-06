package com.dtsx.astra.cli.gateways.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;

import java.util.Optional;
import java.util.UUID;

// TODO cache default keyspace
public interface DbCache {
    void cacheDbId(String dbName, UUID id);
    void cacheDbRegion(UUID id, RegionName region);
    Optional<UUID> lookupDbId(DbRef ref);
    Optional<RegionName> lookupDbRegion(DbRef ref);

    default DbRef convertDbNameToIdIfCached(DbRef ref) {
        return ref.fold(
            _ -> ref,
            _ -> lookupDbId(ref).map(DbRef::fromId).orElse(ref)
        );
    }
}
