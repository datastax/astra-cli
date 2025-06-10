package com.dtsx.astra.cli.gateways.db;

import com.dtsx.astra.cli.core.models.DbRef;

import java.util.Optional;
import java.util.UUID;

public interface DbCache {
    void cacheDbId(String dbName, UUID id);
    void cacheDbRegion(UUID id, String region);
    Optional<UUID> lookupDbId(DbRef ref);
    Optional<String> lookupDbRegion(DbRef ref);

    default DbRef convertDbNameToIdIfCached(DbRef ref) {
        return ref.fold(
            _ -> ref,
            _ -> lookupDbId(ref).map(DbRef::fromId).orElse(ref)
        );
    }
}
