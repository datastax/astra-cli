package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.config.ProfileName;

import java.io.File;

public class DbCompletionsCache extends ProfileLinkedCompletionsCache {
    public DbCompletionsCache(ProfileName profileName) {
        super(profileName);
    }

    @Override
    protected File useCacheFile() {
        return new File(useCacheDir(), "db_names");
    }
}
