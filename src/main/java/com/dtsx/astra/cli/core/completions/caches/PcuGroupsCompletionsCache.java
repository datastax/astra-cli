package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileContext;

public class PcuGroupsCompletionsCache extends ProfileLinkedCompletionsCache {
    public PcuGroupsCompletionsCache(CliContext ctx, ProfileContext profileCtx) {
        super(ctx, profileCtx);
    }

    @Override
    protected String useCacheFileName() {
        return "pcu_groups";
    }
}
