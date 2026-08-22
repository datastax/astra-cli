package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileContext;

public class TenantCompletionsCache extends ProfileLinkedCompletionsCache {
    public TenantCompletionsCache(CliContext ctx, ProfileContext profileCtx) {
        super(ctx, profileCtx);
    }

    @Override
    protected String useCacheFileName() {
        return "tenant_names";
    }
}
