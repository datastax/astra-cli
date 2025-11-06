package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.core.config.ProfileName;
import org.graalvm.collections.Pair;

import java.util.Optional;

public class DbCompletionsCache extends ProfileLinkedCompletionsCache {
    public DbCompletionsCache(CliContext ctx, ProfileSource profileSource) {
        super(ctx, profileSource);
    }

    @Override
    protected String useCacheFileName() {
        return "db_names";
    }
}
