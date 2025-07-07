package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.completions.CompletionsCache;

import java.io.File;
import java.util.Optional;

public class RoleCompletionsCache extends CompletionsCache {
    @Override
    protected Optional<File> useCacheFile() {
        return super.useCacheDir().map((dir) -> new File(dir, "role_names"));
    }
}
