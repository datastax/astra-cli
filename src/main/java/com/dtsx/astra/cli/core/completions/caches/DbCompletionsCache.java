package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.core.config.ProfileName;

import java.util.Optional;

public class DbCompletionsCache extends ProfileLinkedCompletionsCache {
    public DbCompletionsCache(CliContext ctx, Optional<ProfileName> profileName) {
        super(ctx, profileName);
    }

    @Override
    protected String useCacheFile() {
        return "db_names";
    }
}
