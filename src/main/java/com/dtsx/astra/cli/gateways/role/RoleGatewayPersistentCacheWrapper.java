package com.dtsx.astra.cli.gateways.role;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.sdk.org.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public class RoleGatewayPersistentCacheWrapper implements RoleGateway {
    private final CliContext ctx;
    private final RoleGateway delegate;
    private final Supplier<Path> cacheDir;

    @Override
    public Stream<Role> findAll() {
        val roles = delegate.findAll().toList();
        val newMappings = new HashMap<String, String>();

        for (val role : roles) {
            newMappings.put(role.getId(), role.getName());
        }

        saveCache(loadCache(), newMappings);
        return roles.stream();
    }

    @Override
    public Optional<Role> tryFindOne(RoleRef ref) {
        val maybeRole = delegate.tryFindOne(ref);

        maybeRole.ifPresent((role) -> {
            saveCache(loadCache(), Map.of(role.getId(), role.getName()));
        });

        return maybeRole;
    }

    @Override
    public Map<UUID, Optional<String>> findNames(Set<UUID> ids) {
        val cache = loadCache();

        val res = new HashMap<UUID, Optional<String>>();

        for (val id : ids) {
            val cached = cache.getProperty(id.toString());

            if (cached != null) {
                ctx.log().debug("Role name for id ", id.toString(), " found in cache: ", cached);
                res.put(id, Optional.of(cached));
            } else {
                ctx.log().debug("Role name for id ", id.toString(), " not found in cache");
            }
        }

        val idsToFetch = ids.stream()
            .filter(id -> !res.containsKey(id))
            .collect(toSet());

        if (!idsToFetch.isEmpty()) {
            val fetched = delegate.findNames(idsToFetch);
            res.putAll(fetched);

            val newMappings = new HashMap<String, String>();

            for (val entry : fetched.entrySet()) {
                entry.getValue().ifPresent(name -> newMappings.put(entry.getKey().toString(), name));
            }

            saveCache(cache, newMappings);
        }

        return res;
    }

    @SneakyThrows
    private Properties loadCache() {
        val cacheFile = cacheFile();

        if (!Files.exists(cacheFile)) {
            return new Properties();
        }

        try {
            val cache = new Properties();
            cache.load(Files.newBufferedReader(cacheFile));
            return cache;
        } catch (Exception e) {
            ctx.log().exception("Error loading role id to name cache", e);
            return new Properties();
        }
    }

    @SneakyThrows
    private void saveCache(Properties oldCache, Map<String, String> newMappings) {
        val merged = new Properties();
        merged.putAll(oldCache);
        merged.putAll(newMappings);

        if (merged.equals(oldCache)) {
            return;
        }

        val cacheFile = cacheFile();

        try {
            ctx.log().debug("Updating role id to name cache at ", cacheFile.toString());
            merged.store(Files.newBufferedWriter(cacheFile), null);
        } catch (Exception e) {
            ctx.log().exception("Error updating role id to name cache, deleting cache file", e);
            try {
                Files.deleteIfExists(cacheFile);
            } catch (Exception _) {}
        }
    }

    private Path cacheFile() {
        return cacheDir.get().resolve("role_ids_to_names.properties");
    }
}
