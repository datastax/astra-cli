package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.CompletionsCache;

public class RoleCompletionsCache extends CompletionsCache {
    public RoleCompletionsCache(CliContext ctx) {
        super(ctx);
    }

    @Override
    protected String useCacheFile() {
        return "role_names";
    }
}
