package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.CompletionsCache;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class RoleCompletionsCache extends CompletionsCache {
    public RoleCompletionsCache(CliContext ctx) {
        super(ctx);
    }

    @Override
    protected Optional<Path> primaryCacheFile() {
        return Optional.of(defaultCacheDir(ctx).resolve("role_names"));
    }

    @Override
    protected List<Path> mirrorCacheFiles() {
        return List.of();
    }
}
