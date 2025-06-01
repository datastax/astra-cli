package com.dtsx.astra.cli.completions.caches;

import com.dtsx.astra.cli.completions.ProfileLinkedCompletionsCache;

import java.io.File;

public class DbCompletionsCache extends ProfileLinkedCompletionsCache {
    public DbCompletionsCache(String profileName) {
        super(profileName);
    }

    @Override
    protected File getCacheFile() {
        return new File(getCacheDir(), "db_names");
    }
}
