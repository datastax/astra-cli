package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.completions.CompletionsCache;

import java.nio.file.Path;
import java.util.Optional;

public class RoleCompletionsCache extends CompletionsCache {
    @Override
    protected Optional<Path> useCacheFile() {
        return super.useCacheDir().map((dir) -> dir.resolve("role_names"));
    }
}
