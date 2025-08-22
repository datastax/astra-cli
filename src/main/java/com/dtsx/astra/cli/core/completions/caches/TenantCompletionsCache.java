package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;

import java.io.File;
import java.util.Optional;

public class TenantCompletionsCache extends ProfileLinkedCompletionsCache {
    public TenantCompletionsCache(Optional<ProfileName> profileName) {
        super(profileName);
    }

    @Override
    protected Optional<File> useCacheFile() {
        return super.useCacheDir().map((dir) -> new File(dir, "tenant_names"));
    }
}
