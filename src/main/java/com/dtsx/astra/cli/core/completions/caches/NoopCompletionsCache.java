package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.CompletionsCache;

import java.nio.file.Path;
import java.util.Optional;

public class NoopCompletionsCache extends CompletionsCache {
    public static final CompletionsCache INSTANCE = new NoopCompletionsCache(null);

    private NoopCompletionsCache(CliContext ctx) {
        super(ctx);
    }

    @Override
    protected Optional<Path> useCacheDir() {
        return Optional.empty();
    }

    @Override
    protected String useCacheFileName() {
        return null;
    }
}
