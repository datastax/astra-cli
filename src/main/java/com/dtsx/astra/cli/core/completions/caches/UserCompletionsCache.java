package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileContext;

public class UserCompletionsCache extends ProfileLinkedCompletionsCache {
    public UserCompletionsCache(CliContext ctx, ProfileContext profileCtx) {
        super(ctx, profileCtx);
    }

    @Override
    protected String useCacheFileName() {
        return "user_emails";
    }
}
