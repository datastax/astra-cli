package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.core.config.Profile;
import org.apache.commons.lang3.tuple.Pair;

public class PcuGroupsCompletionsCache extends ProfileLinkedCompletionsCache {
    public PcuGroupsCompletionsCache(CliContext ctx,Pair<Profile, ProfileSource> profileAndSource) {
        super(ctx, profileAndSource);
    }

    @Override
    protected String useCacheFileName() {
        return "pcu_groups";
    }
}
