package com.dtsx.astra.cli.gateways.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.val;

import java.util.Optional;
import java.util.UUID;

import static com.datastax.astra.client.core.options.DataAPIClientOptions.DEFAULT_KEYSPACE;

// TODO cache default keyspace
public interface DbCache {
    void cacheDbId(String dbName, UUID id);
    void cacheDbRegion(UUID id, RegionName region);
    void cacheDbDefaultKs(UUID id, String keyspace);

    default void cache(Database db) {
        val id = UUID.fromString(db.getId());

        cacheDbId(db.getInfo().getName(), id);
        cacheDbRegion(id, RegionName.mkUnsafe(db.getInfo().getRegion()));

        val keyspaces = db.getInfo().getKeyspaces();

        if (keyspaces != null) {
            if (keyspaces.size() == 1) {
                cacheDbDefaultKs(id, keyspaces.stream().toList().getFirst());
            } else if (keyspaces.contains(DEFAULT_KEYSPACE)) {
                cacheDbDefaultKs(id, DEFAULT_KEYSPACE);
            }
        }
    }

    Optional<UUID> lookupDbId(DbRef ref);
    Optional<RegionName> lookupDbRegion(DbRef ref);
    Optional<String> lookupDbDefaultKs(DbRef ref);

    default DbRef convertDbNameToIdIfCached(DbRef ref) {
        return ref.fold(
            _ -> ref,
            _ -> lookupDbId(ref).map(DbRef::fromId).orElse(ref)
        );
    }
}
