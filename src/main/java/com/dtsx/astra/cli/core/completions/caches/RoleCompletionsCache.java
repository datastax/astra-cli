package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.completions.CompletionsCache;

public class RoleCompletionsCache extends CompletionsCache {
    @Override
    protected String useCacheFile() {
        return "role_names";
    }
}
