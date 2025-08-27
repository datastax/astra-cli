package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.core.config.ProfileName;

import java.util.Optional;

public class TenantCompletionsCache extends ProfileLinkedCompletionsCache {
    public TenantCompletionsCache(Optional<ProfileName> profileName) {
        super(profileName);
    }

    @Override
    protected String useCacheFile() {
        return "tenant_names";
    }
}
