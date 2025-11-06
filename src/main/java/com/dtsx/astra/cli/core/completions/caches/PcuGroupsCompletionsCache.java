package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;

public class PcuGroupsCompletionsCache extends ProfileLinkedCompletionsCache {
    public PcuGroupsCompletionsCache(CliContext ctx, ProfileSource profileSource) {
        super(ctx, profileSource);
    }

    @Override
    protected String useCacheFileName() {
        return "pcu_groups";
    }
}
