package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.core.config.ProfileName;

import java.nio.file.Path;
import java.util.Optional;

public class TenantCompletionsCache extends ProfileLinkedCompletionsCache {
    public TenantCompletionsCache(Optional<ProfileName> profileName) {
        super(profileName);
    }

    @Override
    protected Optional<Path> useCacheFile() {
        return super.useCacheDir().map((dir) -> dir.resolve("tenant_names"));
    }
}
